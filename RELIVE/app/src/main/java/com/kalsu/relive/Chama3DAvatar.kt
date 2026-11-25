package com.kalsu.relive

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Chama3DAvatar(
    gesture: String,
    speaking: Boolean,
    viseme: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ChamaGLView(context).apply {
                setGesture(gesture)
                setSpeaking(speaking, viseme)
            }
        },
        update = { view ->
            view.setGesture(gesture)
            view.setSpeaking(speaking, viseme)
        }
    )
}

private class ChamaGLView(context: Context) : GLSurfaceView(context) {
    private val avatarRenderer = ChamaRenderer()
    init {
        setEGLContextClientVersion(2)
        setRenderer(avatarRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        preserveEGLContextOnPause = true
    }
    fun setGesture(value: String) { queueEvent { avatarRenderer.gesture = value } }
    fun setSpeaking(value: Boolean, shape: String) {
        queueEvent { avatarRenderer.speaking = value; avatarRenderer.viseme = shape }
    }
    override fun onPause() { super.onPause() }
    override fun onResume() { super.onResume() }
}

private class ChamaRenderer : GLSurfaceView.Renderer {
    @Volatile var gesture = "idle"
    @Volatile var speaking = false
    @Volatile var viseme = "REST"
    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var mvpHandle = 0
    private var scaleHandle = 0
    private var vertexBuffer: FloatBuffer? = null
    private var vertexCount = 0
    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val model = FloatArray(16)
    private val vp = FloatArray(16)
    private val mvp = FloatArray(16)
    private val vertices = 24 * 24 * 6

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.055f, 0.035f, 0.075f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        val vertexShader = """
            uniform mat4 uMVP;
            uniform vec3 uScale;
            attribute vec3 aPosition;
            varying vec3 vNormal;
            void main() {
              vNormal = normalize(aPosition / max(uScale, vec3(0.001)));
              gl_Position = uMVP * vec4(aPosition, 1.0);
            }
        """.trimIndent()
        val fragmentShader = """
            precision mediump float;
            uniform vec4 uColor;
            varying vec3 vNormal;
            void main() {
              vec3 n = normalize(vNormal);
              vec3 light = normalize(vec3(-0.35, 0.75, 0.85));
              float diffuse = max(dot(n, light), 0.0);
              float specular = pow(max(dot(reflect(-light, n), vec3(0.0, 0.0, 1.0)), 0.0), 22.0);
              float rim = pow(1.0 - max(n.z, 0.0), 2.0) * 0.12;
              vec3 lit = uColor.rgb * (0.42 + diffuse * 0.62 + rim) + vec3(1.0, 0.82, 0.66) * specular * 0.16;
              gl_FragColor = vec4(lit, uColor.a);
            }
        """.trimIndent()
        program = linkProgram(compileShader(GLES20.GL_VERTEX_SHADER, vertexShader), compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader))
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        mvpHandle = GLES20.glGetUniformLocation(program, "uMVP")
        scaleHandle = GLES20.glGetUniformLocation(program, "uScale")
        vertexBuffer = makeSphere()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Matrix.perspectiveM(projection, 0, 34f, width.toFloat() / height.coerceAtLeast(1), 0.1f, 100f)
        Matrix.setLookAtM(view, 0, 0f, 0.2f, 8.8f, 0f, 0.15f, 0f, 0f, 1f, 0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)
        GLES20.glEnableVertexAttribArray(positionHandle)
        vertexBuffer?.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
        Matrix.multiplyMM(vp, 0, projection, 0, view, 0)
        val t = System.nanoTime() / 1_000_000_000.0
        val breath = sin(t * 2.0).toFloat() * 0.025f
        val wave = if (gesture == "wave") sin(t * 5.5).toFloat() * 28f else 0f
        val dance = if (gesture == "dance") sin(t * 4.0).toFloat() * 9f else 0f
        val blink = if (sin(t * 0.73) > 0.985) 0.008f else 0.035f
        val mouthOpen = if (!speaking) 0.012f else when (viseme) { "A" -> 0.075f; "O" -> 0.060f; "I", "E" -> 0.028f; "U", "WQ" -> 0.045f; "MBP", "FV" -> 0.008f; else -> 0.025f + (sin(t * 15.0).toFloat().coerceAtLeast(0f) * 0.025f) }

        // Full-body stylized 3D companion: white heart sweater, long braids and warm skin.
        draw(0f, -1.05f, 0f, 0.58f, 0.68f, 0.34f, 0.10f, 0.08f, 0.06f) // skirt
        draw(0f, -0.22f + breath, 0f, 0.67f, 0.78f, 0.38f, 0.94f, 0.91f, 0.88f) // sweater
        draw(-0.02f, 0.70f + breath, 0f, 0.43f, 0.48f, 0.38f, 0.48f, 0.27f, 0.20f) // neck/head base
        draw(-0.02f, 1.55f + breath, 0f, 0.54f, 0.67f, 0.47f, 0.48f, 0.27f, 0.20f) // face
        draw(-0.02f, 2.03f + breath, -0.02f, 0.55f, 0.30f, 0.48f, 0.035f, 0.03f, 0.045f) // braided cap
        // Long dark-brown braids, falling over both shoulders.
        for (i in 0..5) {
            val x = -0.42f + i * 0.16f
            val offset = if (i < 3) -0.10f else 0.10f
            draw(x + offset, 0.55f, 0.12f, 0.055f, 0.95f, 0.06f, 0.16f, 0.075f, 0.045f, i * 5f)
            draw(x + offset * 1.2f, -0.34f, 0.12f, 0.052f, 0.72f, 0.055f, 0.20f, 0.10f, 0.055f, -i * 3f)
        }
        // Ears and subtle warm inner-ear details.
        draw(-0.535f, 1.53f, 0.02f, 0.105f, 0.17f, 0.10f, 0.43f, 0.22f, 0.17f)
        draw(0.495f, 1.53f, 0.02f, 0.105f, 0.17f, 0.10f, 0.43f, 0.22f, 0.17f)
        draw(-0.545f, 1.52f, 0.105f, 0.035f, 0.075f, 0.025f, 0.62f, 0.34f, 0.27f)
        draw(0.505f, 1.52f, 0.105f, 0.035f, 0.075f, 0.025f, 0.62f, 0.34f, 0.27f)
        // Layered eyes: ivory sclera, dark irises, pupils and catchlights.
        draw(-0.22f, 1.62f, 0.414f, 0.092f, blink, 0.035f, 0.96f, 0.91f, 0.83f)
        draw(0.18f, 1.62f, 0.414f, 0.092f, blink, 0.035f, 0.96f, 0.91f, 0.83f)
        draw(-0.22f, 1.62f, 0.445f, 0.039f, blink * 0.78f, 0.018f, 0.20f, 0.105f, 0.065f)
        draw(0.18f, 1.62f, 0.445f, 0.039f, blink * 0.78f, 0.018f, 0.20f, 0.105f, 0.065f)
        draw(-0.22f, 1.62f, 0.461f, 0.018f, blink * 0.55f, 0.012f, 0.035f, 0.025f, 0.022f)
        draw(0.18f, 1.62f, 0.461f, 0.018f, blink * 0.55f, 0.012f, 0.035f, 0.025f, 0.022f)
        draw(-0.232f, 1.641f, 0.474f, 0.010f, 0.010f, 0.007f, 1f, 0.98f, 0.91f)
        draw(0.168f, 1.641f, 0.474f, 0.010f, 0.010f, 0.007f, 1f, 0.98f, 0.91f)
        // Eyelids, brows and soft nose bridge.
        draw(-0.22f, 1.705f, 0.414f, 0.105f, 0.025f, 0.035f, 0.12f, 0.055f, 0.045f, -4f)
        draw(0.18f, 1.705f, 0.414f, 0.105f, 0.025f, 0.035f, 0.12f, 0.055f, 0.045f, 4f)
        draw(-0.22f, 1.755f, 0.405f, 0.105f, 0.035f, 0.035f, 0.20f, 0.105f, 0.075f, -7f)
        draw(0.18f, 1.755f, 0.405f, 0.105f, 0.035f, 0.035f, 0.20f, 0.105f, 0.075f, 7f)
        draw(-0.02f, 1.52f, 0.445f, 0.035f, 0.16f, 0.035f, 0.53f, 0.31f, 0.24f)
        draw(-0.02f, 1.425f, 0.469f, 0.075f, 0.055f, 0.045f, 0.58f, 0.34f, 0.27f)
        draw(-0.055f, 1.414f, 0.507f, 0.012f, 0.008f, 0.008f, 0.30f, 0.13f, 0.11f)
        draw(0.015f, 1.414f, 0.507f, 0.012f, 0.008f, 0.008f, 0.30f, 0.13f, 0.11f)
        // Mouth cavity and separate upper/lower lips for clearer viseme shapes.
        draw(-0.02f, 1.285f, 0.468f, 0.112f, mouthOpen, 0.024f, 0.19f, 0.025f, 0.045f)
        draw(-0.02f, 1.305f, 0.491f, 0.105f, 0.025f + mouthOpen * 0.20f, 0.018f, 0.55f, 0.20f, 0.20f)
        draw(-0.02f, 1.255f, 0.492f, 0.082f, 0.018f + mouthOpen * 0.18f, 0.018f, 0.69f, 0.29f, 0.29f)
        // Tiny gold earrings and sweater collar.
        draw(-0.55f, 1.36f, 0.11f, 0.035f, 0.045f, 0.025f, 0.84f, 0.66f, 0.30f)
        draw(0.52f, 1.36f, 0.11f, 0.035f, 0.045f, 0.025f, 0.84f, 0.66f, 0.30f)
        draw(-0.02f, 0.48f, 0.365f, 0.28f, 0.09f, 0.045f, 0.78f, 0.75f, 0.72f)
        // Arms: raised and waving when greeted.
        draw(-0.70f, 0.05f, 0f, 0.20f, 0.58f, 0.20f, 0.94f, 0.91f, 0.88f, -18f + dance)
        draw(0.68f, 0.02f, 0f, 0.20f, 0.58f, 0.20f, 0.94f, 0.91f, 0.88f, 18f - dance)
        draw(-0.90f, 0.62f + wave / 90f, 0f, 0.17f, 0.22f, 0.16f, 0.48f, 0.27f, 0.20f, wave)
        draw(0.84f, -0.20f, 0f, 0.17f, 0.22f, 0.16f, 0.48f, 0.27f, 0.20f, 0f)
        // Red sweater hearts.
        draw(-0.28f, 0.10f, 0.355f, 0.15f, 0.12f, 0.035f, 0.86f, 0.035f, 0.08f, -12f)
        draw(0.22f, -0.20f, 0.355f, 0.16f, 0.13f, 0.035f, 0.86f, 0.035f, 0.08f, 10f)
        draw(0.03f, -0.48f, 0.355f, 0.14f, 0.11f, 0.035f, 0.86f, 0.035f, 0.08f, -8f)
        // Legs and shoes.
        draw(-0.28f, -1.72f, 0f, 0.22f, 0.76f, 0.23f, 0.48f, 0.27f, 0.20f, -4f + dance)
        draw(0.28f, -1.72f, 0f, 0.22f, 0.76f, 0.23f, 0.48f, 0.27f, 0.20f, 4f - dance)
        draw(-0.28f, -2.42f, 0.12f, 0.26f, 0.16f, 0.38f, 0.95f, 0.93f, 0.91f)
        draw(0.28f, -2.42f, 0.12f, 0.26f, 0.16f, 0.38f, 0.95f, 0.93f, 0.91f)
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    private fun draw(x: Float, y: Float, z: Float, sx: Float, sy: Float, sz: Float,
                     r: Float, g: Float, b: Float, rotation: Float = 0f) {
        Matrix.setIdentityM(model, 0)
        Matrix.translateM(model, 0, x, y, z)
        Matrix.rotateM(model, 0, rotation, 0f, 0f, 1f)
        Matrix.scaleM(model, 0, sx, sy, sz)
        Matrix.multiplyMM(mvp, 0, vp, 0, model, 0)
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0)
        GLES20.glUniform3f(scaleHandle, sx, sy, sz)
        GLES20.glUniform4f(colorHandle, r, g, b, 1f)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
    }

    private fun makeSphere(): FloatBuffer {
        val data = ArrayList<Float>()
        val lat = 18
        val lon = 24
        for (i in 0 until lat) {
            val a0 = -PI / 2 + PI * i / lat
            val a1 = -PI / 2 + PI * (i + 1) / lat
            for (j in 0 until lon) {
                val b0 = 2 * PI * j / lon
                val b1 = 2 * PI * (j + 1) / lon
                val p00 = point(a0, b0); val p10 = point(a1, b0)
                val p01 = point(a0, b1); val p11 = point(a1, b1)
                listOf(p00, p10, p01, p01, p10, p11).forEach { point -> point.forEach { value -> data.add(value) } }
            }
        }
        vertexCount = data.size / 3
        return ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply { data.forEach { put(it) }; position(0) }
    }

    private fun point(lat: Double, lon: Double) = floatArrayOf(
        (cos(lat) * cos(lon)).toFloat(),
        sin(lat).toFloat(),
        (cos(lat) * sin(lon)).toFloat()
    )

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        return shader
    }

    private fun linkProgram(vertex: Int, fragment: Int): Int {
        val p = GLES20.glCreateProgram()
        GLES20.glAttachShader(p, vertex)
        GLES20.glAttachShader(p, fragment)
        GLES20.glLinkProgram(p)
        return p
    }
}
