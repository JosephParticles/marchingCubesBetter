package gameLogic;

import gameLogic.PerlinNoise.PerlinNoise;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryUtil;
import rendering.Mesh;
import rendering.TerrainChunk;

import java.nio.FloatBuffer;
import java.util.Arrays;

import static org.joml.Math.signum;

public class Generator {
    private final TriangulationTable triangulationTable;
    private final PerlinNoise noise;

    public Generator(){
        triangulationTable=new TriangulationTable();
        noise=new PerlinNoise(10);
    }

    private float square(float a){
        return a*a;
    }


    public TerrainChunk[] generateTerrainChunks(float[] scalarField, int size, int chunkSize){
        int chunksNumber=(int)Math.ceil((float)size/chunkSize);
        TerrainChunk[] chunks=new TerrainChunk[chunksNumber*chunksNumber*chunksNumber];
        int count=0;
        for(int x=0;x<chunksNumber;x++){
            for(int y=0;y<chunksNumber;y++){
                for(int z=0;z<chunksNumber;z++){
                    chunks[count]=new TerrainChunk(new Vector3i(x*chunkSize,y*chunkSize,z*chunkSize),this,chunkSize);
                    chunks[count].calcLinearMesh(scalarField,size);
                    count++;
                }
            }
        }
        return chunks;
    }

    private float normalise(float a, float b, float f){
        if(a==b){
            return 0;
        }
        return (f-a)/(b-a);
    }

    private Vector3f interpolateXEdge(Vector3f aPos, Vector3f bPos, float a, float b){
        return new Vector3f(aPos.x+normalise(a,b,0)*signum(bPos.x-aPos.x),aPos.y,aPos.z);
    }

    private Vector3f interpolateYEdge(Vector3f aPos, Vector3f bPos, float a, float b){
        return new Vector3f(aPos.x,aPos.y+normalise(a,b,0)*signum(bPos.y-aPos.y),aPos.z);
    }

    private Vector3f interpolateZEdge(Vector3f aPos, Vector3f bPos, float a, float b){
        return new Vector3f(aPos.x,aPos.y,aPos.z+normalise(a,b,0)*signum(bPos.z-aPos.z));
    }

    public Mesh calcMesh(float[] scalarField, Vector3i offset, int chunkSize, int size){
        float[] triangles=new float[45*chunkSize*chunkSize*chunkSize];
        int triangleCount=0;
        for(int x=offset.x;x<=offset.x+chunkSize;x++){
            for(int y=offset.y;y<=offset.y+chunkSize;y++){
                for(int z=offset.z;z<=offset.z+chunkSize;z++){
                    float[] values=new float[]{
                            scalarField[(x*(size+1)+y)*(size+1)+z],
                            scalarField[((x+1)*(size+1)+y)*(size+1)+z],
                            scalarField[((x+1)*(size+1)+y)*(size+1)+(z+1)],
                            scalarField[(x*(size+1)+y)*(size+1)+(z+1)],
                            scalarField[(x*(size+1)+(y+1))*(size+1)+z],
                            scalarField[((x+1)*(size+1)+(y+1))*(size+1)+z],
                            scalarField[((x+1)*(size+1)+(y+1))*(size+1)+(z+1)],
                            scalarField[(x*(size+1)+(y+1))*(size+1)+(z+1)]
                    };

//                    if(values[0]!=0){
//                        System.out.println(values[0]);
//                    }

                    int voxelIndex=0;
                    if(values[0]>=0){voxelIndex+=1;}
                    if(values[1]>=0){voxelIndex+=2;}
                    if(values[2]>=0){voxelIndex+=4;}
                    if(values[3]>=0){voxelIndex+=8;}
                    if(values[4]>=0){voxelIndex+=16;}
                    if(values[5]>=0){voxelIndex+=32;}
                    if(values[6]>=0){voxelIndex+=64;}
                    if(values[7]>=0){voxelIndex+=128;}

                    int[] indices= triangulationTable.getTriangle(voxelIndex);
                    int edgeIndex= triangulationTable.getEdges(voxelIndex);

                    Vector3f[] edges=new Vector3f[12];
                    if((edgeIndex&1)==1){
                        edges[0]=interpolateXEdge(new Vector3f(x,y,z),new Vector3f(x+1,y,z),values[0],values[1]);//x
                    }if((edgeIndex&2)==2){
                        edges[1]=interpolateZEdge(new Vector3f(x+1,y,z),new Vector3f(x+1,y,z+1),values[1],values[2]);//z
                    }if((edgeIndex&4)==4){
                        edges[2]=interpolateXEdge(new Vector3f(x+1,y,z+1),new Vector3f(x,y,z+1),values[2],values[3]);//x
                    }if((edgeIndex&8)==8){
                        edges[3]=interpolateZEdge(new Vector3f(x,y,z+1),new Vector3f(x,y,z),values[3],values[0]);//z
                    }if((edgeIndex&16)==16){
                        edges[4]=interpolateXEdge(new Vector3f(x,y+1,z),new Vector3f(x+1,y+1,z),values[4],values[5]);//x
                    }if((edgeIndex&32)==32){
                        edges[5]=interpolateZEdge(new Vector3f(x+1,y+1,z),new Vector3f(x+1,y+1,z+1),values[5],values[6]);//z
                    }if((edgeIndex&64)==64){
                        edges[6]=interpolateXEdge(new Vector3f(x+1,y+1,z+1),new Vector3f(x,y+1,z+1),values[6],values[7]);//x
                    }if((edgeIndex&128)==128){
                        edges[7]=interpolateZEdge(new Vector3f(x,y+1,z+1),new Vector3f(x,y+1,z),values[7],values[4]);//z
                    }if((edgeIndex&256)==256){
                        edges[8]=interpolateYEdge(new Vector3f(x,y,z),new Vector3f(x,y+1,z),values[0],values[4]);//y
                    }if((edgeIndex&512)==512){
                        edges[9]=interpolateYEdge(new Vector3f(x+1,y,z),new Vector3f(x+1,y+1,z),values[1],values[5]);//y
                    }if((edgeIndex&1024)==1024){
                        edges[10]=interpolateYEdge(new Vector3f(x+1,y,z+1),new Vector3f(x+1,y+1,z+1),values[2],values[6]);//y
                    }if((edgeIndex&2048)==2048){
                        edges[11]=interpolateYEdge(new Vector3f(x,y,z+1),new Vector3f(x,y+1,z+1),values[3],values[7]);//y
                    }

                    for(int i=0;indices[i]!=15;i+=3){
                        Vector3f vertexA=edges[indices[i]];
                        Vector3f vertexB=edges[indices[i+1]];
                        Vector3f vertexC=edges[indices[i+2]];

                        triangles[triangleCount]=vertexA.x;
                        triangles[triangleCount+1]=vertexA.y;
                        triangles[triangleCount+2]=vertexA.z;

                        triangles[triangleCount+3]=vertexB.x;
                        triangles[triangleCount+4]=vertexB.y;
                        triangles[triangleCount+5]=vertexB.z;

                        triangles[triangleCount+6]=vertexC.x;
                        triangles[triangleCount+7]=vertexC.y;
                        triangles[triangleCount+8]=vertexC.z;

                        triangleCount+=9;
                    }
                }
            }
        }
        FloatBuffer buffer= MemoryUtil.memAllocFloat(triangleCount);
        buffer.put(Arrays.copyOf(triangles,triangleCount)).flip();

        int vertexBuffer= GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER,vertexBuffer);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER,buffer,GL46.GL_STATIC_DRAW);

        Mesh mesh=new Mesh(vertexBuffer,triangleCount);
        MemoryUtil.memFree(buffer);

        return mesh;
    }
}
