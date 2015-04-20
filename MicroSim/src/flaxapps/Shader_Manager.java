package flaxapps;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;

import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;


public class Shader_Manager {
	
	private void checkLogInfo(GL2 gl, int programObject) {
        IntBuffer intValue = Buffers.newDirectIntBuffer(1);

        gl.glGetObjectParameterivARB(programObject, GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB, intValue);

        int lengthWithNull = intValue.get();

        if (lengthWithNull <= 1) {
            return;
        }

        ByteBuffer infoLog = Buffers.newDirectByteBuffer(lengthWithNull);

        intValue.flip();
        gl.glGetInfoLogARB(programObject, lengthWithNull, intValue, infoLog);
        
        int actualLength = intValue.get();

        byte[] infoBytes = new byte[actualLength];
        infoLog.get(infoBytes);
        System.out.println("GLSL Validation >> " + new String(infoBytes));
    }
	
	public int init(String name,GL2 gl) throws IOException {
		int v = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
		int f = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
		
		URL u = MicroSimulation.class.getResource(name + ".vert");
		
		String vsrc = readFromStream(u.openStream());
		gl.glShaderSource(v, 1, new String[] { vsrc }, (int[]) null, 0);
		gl.glCompileShader(v);
		
		int[] result = new int[1];
		gl.glGetShaderiv(v, GL2.GL_COMPILE_STATUS, result, 0);
		if(result[0] != 1){
			checkLogInfo(gl,v);
		}
		
		u = MicroSimulation.class.getResource(name + ".frag");
		
		String fsrc = readFromStream(u.openStream());
		gl.glShaderSource(f, 1, new String[] { fsrc }, (int[]) null, 0);
		gl.glCompileShader(f);

		result = new int[1];
		gl.glGetShaderiv(f, GL2.GL_COMPILE_STATUS, result, 0);
		if(result[0] != 1){
			checkLogInfo(gl,f);
		}
		
		int shaderprogram = gl.glCreateProgram();
		gl.glAttachShader(shaderprogram, v);
		gl.glAttachShader(shaderprogram, f);
		gl.glLinkProgram(shaderprogram);
		gl.glValidateProgram(shaderprogram);

		return shaderprogram;
	}
	

	public static String readFromStream(InputStream ins) throws IOException {
		if (ins == null) {
			throw new IOException("Could not read from stream.");
		}
		StringBuffer buffer = new StringBuffer();
		Scanner scanner = new Scanner(ins);
		try {
			while (scanner.hasNextLine()) {
				buffer.append(scanner.nextLine() + "\n");
			}
		} finally {
			scanner.close();
		}

		return buffer.toString();
	}
}

