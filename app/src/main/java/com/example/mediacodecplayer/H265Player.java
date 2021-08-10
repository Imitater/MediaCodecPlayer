package com.example.mediacodecplayer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class H265Player {
    private static final String TAG = "H264Player";
    private MediaCodec mediaCodec;
    private int width = 720;
    private int height = 1280;

    public  H265Player(Surface suface) {
        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC);
            final MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height);
            format.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
            mediaCodec.configure(format, suface, null, 0);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dealInfo(byte[] data) {
        int index = mediaCodec.dequeueInputBuffer(100000);
        if (index >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(index);
            inputBuffer.clear();
            inputBuffer.put(data, 0, data.length);
            mediaCodec.queueInputBuffer(index, 0, data.length, System.currentTimeMillis(), 0);
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
        if (outputBufferIndex>=0){
            mediaCodec.releaseOutputBuffer(outputBufferIndex,true);
        }
    }
}
