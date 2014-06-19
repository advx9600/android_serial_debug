#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <malloc.h>
#include <sys/types.h>
#include <fcntl.h>
#include <termio.h>
#include <errno.h>
#include <pthread.h>
#include <poll.h>
#include <signal.h>
#include <android/Log.h>
#define LOG_TAG "dafeng_serial_assistance"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGV(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGT(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

int pin485;
static int init485() {
	pin485 = open("/dev/pin_485", O_RDWR);
	if (pin485 < 0) {
		LOGE("open /dev/pin_485 failed!");
		return -1;
	}
	return 0;
}
static int set485(int opr) {
	if (pin485 > 0) {
		if (ioctl(pin485, opr)) {
			LOGE("ioctl failed!");
		}
	}
	return 0;
}
static void close485() {
	if (pin485 > 0) {
		close(pin485);
	}
}

static int getBaudRate(int baudRate) {
	switch (baudRate) {
	case 0:
		return B0;
	case 50:
		return B50;
	case 110:
		return B110;
	case 134:
		return B134;
	case 150:
		return B150;
	case 200:
		return B200;
	case 300:
		return B300;
	case 600:
		return B600;
	case 1200:
		return B1200;
	case 1800:
		return B1800;
	case 2400:
		return B2400;
	case 4800:
		return B4800;
	case 9600:
		return B9600;
	case 19200:
		return B19200;
	case 38400:
		return B38400;
	case 57600:
		return B57600;
	case 115200:
		return B115200;
	case 230400:
		return B230400;
	}
}
static int pollRead(int sd, int msec) { // timeout or err return 0 ,have data return non-zero
	int ret = 0;
	struct pollfd fds;
	fds.fd = sd;
	fds.events = POLLIN;
	ret = poll(&fds, 1, msec);
	if (ret == -1) {
		perror("poll");
		LOGE("poll read failed!");
		ret = 0;
	}
	return ret;
}
static int openSerial(JNIEnv* env, jobject obj, jstring devName, int baudRate,
		int vTime, int vMin) {
	struct termios option;
	const char *dev = (env)->GetStringUTFChars(devName, 0);
	int fd = open(dev, O_RDWR);
	if (fd < 0) {
		perror("open device");
		LOGE("open %s failed!", dev);
		return -1;
	}
	(env)->ReleaseStringUTFChars(devName, dev);

	bzero(&option, sizeof(option));
	cfmakeraw(&option);

	option.c_cflag |= CLOCAL | CREAD;
	option.c_cflag |= CS8;
	option.c_cflag &= ~PARENB; // no parity check
	option.c_cflag &= ~CSTOPB; // one stop bit
	option.c_cflag &= ~CRTSCTS; //no flow control
	option.c_oflag = 0;
	option.c_lflag |= 0;
	option.c_oflag &= ~OPOST;
	option.c_cc[VTIME] = vTime; /* unit: 1/10 second. */
	option.c_cc[VMIN] = vMin; /* minimal characters for reading */
	tcflush(fd, TCIFLUSH);
	cfsetispeed(&option, getBaudRate(baudRate));
	cfsetospeed(&option, getBaudRate(baudRate));
	if (tcsetattr(fd, TCSANOW, &option)) {
		LOGE("tcsetattr failed!");
	}
	LOGI("open fd:%d", fd);
//	init485();
	return fd;
}
static void closeSerial(JNIEnv* env, jobject obj, int fd) {
	LOGI("close fd:%d", fd);
	if (fd > 0) {
		close(fd);
	}
	close485();
}

static int writeData(JNIEnv* env, jobject obj, jint fd_write, jbyteArray bts,
		jint leng) {
//	LOGI("start writeData");
	if (bts == NULL) {
		LOGE("write bts is NULL!");
		return 0;
	}
	char* bt = (char*) env->GetByteArrayElements(bts, 0);
	const int size = env->GetArrayLength(bts);
//	LOGI("size:%d,leng:%d", size, leng);
	if (size < leng) {
		LOGE("write data size < leng(%d < %d)", size, leng);
		env->ReleaseByteArrayElements(bts, (jbyte*) bt, 0);
		return 0;
	}
//	LOGI("before 485 set 1");
//	set485(0);
//	LOGI("after 485 set 1");
	if (write(fd_write, bt, leng) != leng) {
		LOGE("write data failed!");
		env->ReleaseByteArrayElements(bts, (jbyte*) bt, 0);
		return 0;
	}
//	LOGI("before 485 set 0");
//	set485(1);
//	LOGI("after 485 set 0");
	env->ReleaseByteArrayElements(bts, (jbyte*) bt, 0);
	return leng;
}
static int readData(JNIEnv* env, jobject obj, int fd_read, jbyteArray bts,
		int leng, int timeOut) {
	int i;
	int ret = 0;
	char* bt = (char*) env->GetByteArrayElements(bts, 0);
	int size = env->GetArrayLength(bts);
#if 0
	if (!pollRead(fd_read, timeOut)) {
		env->ReleaseByteArrayElements(bts, (jbyte*) bt, 0);
		return 0;
	}
#endif
//	LOGI("read data size:%d", leng > size ? size : leng);
	ret = read(fd_read, bt, leng > size ? size : leng);
	if (ret < 0) {
		LOGE("read failed!");
	} else if (ret == 0) {
		LOGI("read zero data,serial config err");
	}

	env->ReleaseByteArrayElements(bts, (jbyte*) bt, 0);
	return ret;
}

static const JNINativeMethod gMethods[] = { { "openSerial",
		"(Ljava/lang/String;III)I", (int*) openSerial }, { "closeSerial", "(I)V",
		(void*) closeSerial }, { "writeData", "(I[BI)I", (int*) writeData }, {
		"readData", "(I[BII)I", (int*) readData }, };

static int registerMethods(JNIEnv* env) {
	jclass clazz;
	static const char* const kClassName =
			"com/df/serial/dafeng_serial_debug/SerialController";

	/* look up the class */
	clazz = env->FindClass(kClassName);

	if (clazz == NULL) {
		LOGE("Can't find class %s\n", kClassName);
		return -1;
	}

	/* register all the methods */
	if (env->RegisterNatives(clazz, gMethods,
			sizeof(gMethods) / sizeof(gMethods[0])) != JNI_OK) {
		LOGE("Failed registering methods for %s\n", kClassName);
		return -1;
	}

	/* fill out the rest of the ID cache */
	return 0;
}
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;
	LOGI("--------------JNI_OnLoad---------------------");

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		LOGE("ERROR: GetEnv failed\n");
		goto fail;
	}

//	assert(env != NULL);

	if (registerMethods(env) != 0) {
		LOGE("ERROR: PlatformLibrary native registration failed\n");
		goto fail;
	}

	/* success -- return valid version number */
	result = JNI_VERSION_1_4;
	fail: return result;
	return JNI_VERSION_1_4;
}
