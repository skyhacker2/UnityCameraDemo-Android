using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using System;

public class CameraPlugin : MonoBehaviour {

	Texture2D 	texture;
	int 		textureId;

	AndroidJavaObject nativeObject;

	#if UNITY_ANDROID
	private void _openCamera() {
		nativeObject.Call ("openCamera");
	}
	private void _closeCamera() {
		nativeObject.Call ("closeCamera");
	}
	private bool _isFrameUpdated() {
		return nativeObject.Call<bool> ("isFrameUpdated");
	}
	private int _updateTexture() {
		return nativeObject.Call<int> ("updateTexture");
	}
	private int _getWidth() {
		return nativeObject.Call<int> ("getWidth");
	}
	private int _getHeight() {
		return nativeObject.Call<int> ("getHeight");
	}
	#endif

	void Awake() {
		#if UNITY_ANDROID
		nativeObject = new AndroidJavaObject("io.github.skyhacker2.camera.CameraPlugin");
		if (nativeObject == null) {
			Debug.Log ("Start nativeObject is null");
		}
		#endif
	}

	// Use this for initialization
	void Start () {
		_openCamera ();
	}

	void Stop() {
		_closeCamera ();
	}
	
	// Update is called once per frame
	void Update () {
		if (_isFrameUpdated ()) {
			textureId = _updateTexture ();
			if (texture == null && textureId != 0) {
				Debug.Log ("create external texture");
				texture = Texture2D.CreateExternalTexture (_getWidth(), _getHeight(), TextureFormat.RGB565, false, false, (IntPtr)textureId);
				texture.wrapMode = TextureWrapMode.Clamp;
				texture.filterMode = FilterMode.Bilinear;
			} else if (textureId != 0){
				texture.UpdateExternalTexture ((IntPtr)textureId);
			}

			if (GetComponent<RawImage> () != null) {
				GetComponent<RawImage> ().texture = texture;
				GetComponent<RawImage> ().color = Color.white;
			} else {
				GetComponent<Renderer> ().material.mainTexture = texture;
			}
		}
	}
}
