package flaxapps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL2;

/**
 * @author Danny Flax
 */
public class AnimationHolder{ 
    ArrayList<ModelControl> objs = new ArrayList<ModelControl>();
    int currentFrame = 0;
    int maxFrame = 0;
    
    public void restart(){
        currentFrame = 0;
    }
    public ArrayList<Vertex> drawStillFrame(Vertex pos, GL2 gl, float rangle){
        return objs.get(currentFrame).drawModel(pos, gl, rangle);
        
      
    }
    
    
    public ArrayList<Vertex> drawFrame(Vertex pos, GL2 gl, float rangle){
        ArrayList<Vertex> a = objs.get(currentFrame).drawModel(pos, gl, rangle);
        
        currentFrame++;
        if(currentFrame == maxFrame)
            currentFrame = 0;
        
        return a;
    }
    
    
    public AnimationHolder(String base, int frames){
         //= frames - 1;
        for(int i = 1; i<frames; i=i+2){
         maxFrame++;
            ModelControl ap = new ModelControl();
          String s = base + "_";
          int numZeros = 0;
          if(i<100000)
              numZeros++;
          if(i<10000)
              numZeros++;
          if(i<1000)
              numZeros++;
          if(i<100)
              numZeros++;
          if(i<10)
              numZeros++;
          
          
              for(int i2 = 0; i2<numZeros; i2++){
                  s = s.concat("0");
              }    
                
              s = s.concat(i+".obj");
          
            
              try {
                 ap.loadModelData(s);
                 objs.add(ap);
            } catch (IOException ex) {
                 Logger.getLogger(MicroSimulation.class.getName()).log(Level.SEVERE, null, ex);
            }
         
      }
    }
}
