package rendering;

import gameLogic.Camera;
import gameLogic.Generator;
import org.joml.Vector3i;

public class TerrainChunk {
    private final Vector3i offset;
    private final Generator generator;
    private final int chunkSize;

    private Mesh mesh;
    private boolean hasTriangles;

    public TerrainChunk(Vector3i offset, Generator generator, int chunkSize){
        this.offset=offset;
        this.generator=generator;
        this.chunkSize=chunkSize;
    }

//    public void calcMesh(float[][][] newValues){
//        if(mesh!=null){
//            mesh.cleanup();
//        }
//        mesh=generator.calcMesh(newValues,offset,size-1);
//        hasTriangles=mesh.getVertexCount()!=0;
//    }

    public void calcLinearMesh(float[] newValues,int size){
        if(mesh!=null){
            mesh.cleanup();
        }
        mesh=generator.calcMesh(newValues,offset, chunkSize-1,size);
        hasTriangles=mesh.getVertexCount()!=0;
    }
    
    public int getTriangleCount() {
    	return mesh.getVertexCount()/3;
    }
    
    public boolean hasTriangles(){
    	return hasTriangles;
    }

    public void render(Program program, Camera camera){
        program.setUniform("translation",offset);
        mesh.render(program, camera);
    }

    public void cleanup(){
        mesh.cleanup();
    }
}
