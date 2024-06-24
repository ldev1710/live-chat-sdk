/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class CallMediaInfo {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CallMediaInfo(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CallMediaInfo obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsua2JNI.delete_CallMediaInfo(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setIndex(long value) {
    pjsua2JNI.CallMediaInfo_index_set(swigCPtr, this, value);
  }

  public long getIndex() {
    return pjsua2JNI.CallMediaInfo_index_get(swigCPtr, this);
  }

  public void setType(int value) {
    pjsua2JNI.CallMediaInfo_type_set(swigCPtr, this, value);
  }

  public int getType() {
    return pjsua2JNI.CallMediaInfo_type_get(swigCPtr, this);
  }

  public void setDir(int value) {
    pjsua2JNI.CallMediaInfo_dir_set(swigCPtr, this, value);
  }

  public int getDir() {
    return pjsua2JNI.CallMediaInfo_dir_get(swigCPtr, this);
  }

  public void setStatus(int value) {
    pjsua2JNI.CallMediaInfo_status_set(swigCPtr, this, value);
  }

  public int getStatus() {
    return pjsua2JNI.CallMediaInfo_status_get(swigCPtr, this);
  }

  public void setAudioConfSlot(int value) {
    pjsua2JNI.CallMediaInfo_audioConfSlot_set(swigCPtr, this, value);
  }

  public int getAudioConfSlot() {
    return pjsua2JNI.CallMediaInfo_audioConfSlot_get(swigCPtr, this);
  }

  public void setVideoIncomingWindowId(int value) {
    pjsua2JNI.CallMediaInfo_videoIncomingWindowId_set(swigCPtr, this, value);
  }

  public int getVideoIncomingWindowId() {
    return pjsua2JNI.CallMediaInfo_videoIncomingWindowId_get(swigCPtr, this);
  }

  public void setVideoWindow(VideoWindow value) {
    pjsua2JNI.CallMediaInfo_videoWindow_set(swigCPtr, this, VideoWindow.getCPtr(value), value);
  }

  public VideoWindow getVideoWindow() {
    long cPtr = pjsua2JNI.CallMediaInfo_videoWindow_get(swigCPtr, this);
    return (cPtr == 0) ? null : new VideoWindow(cPtr, false);
  }

  public void setVideoCapDev(int value) {
    pjsua2JNI.CallMediaInfo_videoCapDev_set(swigCPtr, this, value);
  }

  public int getVideoCapDev() {
    return pjsua2JNI.CallMediaInfo_videoCapDev_get(swigCPtr, this);
  }

  public CallMediaInfo() {
    this(pjsua2JNI.new_CallMediaInfo(), true);
  }

}
