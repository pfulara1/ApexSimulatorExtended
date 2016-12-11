package apexsimulatorextended;

public class ROB {
    String destinationRegsiter;
    String physicalRegister;
    int value;
    boolean isValid;
    int pc;
    boolean isBranchTaken;
    int memoryAddressForStore;

    @Override
    public String toString() {
    	return "DestReg:"+destinationRegsiter+"\tPhyReg:"+physicalRegister+
    			"\tValue:"+value+"\tValid:"+isValid+"\tBranch:"+isBranchTaken;
    }
}

