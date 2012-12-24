/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class de_jose_util_WinRegistry */

#ifndef _Included_de_jose_util_WinRegistry
#define _Included_de_jose_util_WinRegistry
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     de_jose_util_WinRegistry
 * Method:    exists_key
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_de_jose_util_WinRegistry_exists_1key
  (JNIEnv *, jclass, jstring);

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    create_key
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_de_jose_util_WinRegistry_create_1key
  (JNIEnv *, jclass, jstring);

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    delete_key
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_de_jose_util_WinRegistry_delete_1key
  (JNIEnv *, jclass, jstring);

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    get_value
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_de_jose_util_WinRegistry_get_1value__Ljava_lang_String_2Ljava_lang_String_2
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    get_value
 * Signature: (Ljava/lang/String;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_de_jose_util_WinRegistry_get_1value__Ljava_lang_String_2I
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    set_string_value
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_de_jose_util_WinRegistry_set_1string_1value
  (JNIEnv *, jclass, jstring, jstring, jstring);

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    delete_value
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_de_jose_util_WinRegistry_delete_1value
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    count_subkeys
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_de_jose_util_WinRegistry_count_1subkeys
  (JNIEnv *, jclass, jstring);

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    count_values
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_de_jose_util_WinRegistry_count_1values
  (JNIEnv *, jclass, jstring);

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    get_value_name
 * Signature: (Ljava/lang/String;I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_de_jose_util_WinRegistry_get_1value_1name
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    get_subkey
 * Signature: (Ljava/lang/String;I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_de_jose_util_WinRegistry_get_1subkey
  (JNIEnv *, jclass, jstring, jint);

#ifdef __cplusplus
}
#endif
#endif