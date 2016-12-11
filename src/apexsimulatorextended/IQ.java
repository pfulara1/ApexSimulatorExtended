package apexsimulatorextended;

public class IQ {
    
   int valuesrc1;
   int valuesrc2;
   boolean src1Valid;
   boolean src2Valid;
   int fuType;
   String src1Tag;
   String src2Tag;
   String destination;
   Instructions ins;  
   int literal;
   
   @Override
   public String toString() {
   	return "InstrType:"+ins.opcode+"\tsrc1value:"+valuesrc1+"\tsrc1valid"+src1Valid+
   			"\tsrc2value:"+valuesrc2+"\tsrc2valid:"+src2Valid+"\tDest:"+destination+"\tFUtype:"+fuType;
   }
}
