package apexsimulatorextended;

public class Instructions {
    

    String opcode = "";
    String destRegister = "";
    String src1Register = "";
    String src2Register = "";
    boolean checkliteral = false;
    int literal = 0;
    String instructionString = "";
    int result;
    String physicalDestRegister = "";
    int pc_value;
    /*
    This method is used for parsing the instruction string and convert it to a instruction class object & return that object 
    */
    public Instructions ProcessInstruction(String instruction, int pc_value) {
        if (instruction != null) {
            instruction = instruction.replace(",", " ");
            String[] Instruction = instruction.split("\\s+");
            this.opcode = Instruction[0];
            if (this.opcode.equals("STORE")) {
                this.src1Register = Instruction[1];
                this.src2Register = Instruction[2];
                this.literal = Integer.parseInt(Instruction[3].substring(1));

            } else if (this.opcode.equals("LOAD")) {
                this.destRegister = Instruction[1];
                this.src1Register = Instruction[2];
                this.literal = Integer.parseInt(Instruction[3].substring(1));
            } else if (this.opcode.equals("MOVC")) {
                this.destRegister = Instruction[1];
                this.literal = Integer.parseInt(Instruction[2].substring(1));
            } else if (this.opcode.equals("JUMP") || this.opcode.equals("BAL")) {
                this.src1Register = Instruction[1];
                this.literal = Integer.parseInt(Instruction[2].substring(1));

            } else if (this.opcode.equals("BZ") || this.opcode.equals("BNZ")) {
                this.literal = Integer.parseInt(Instruction[1].substring(1));
            } else if (this.opcode.equals("HALT")) {

            } else {
                this.destRegister = Instruction[1];
                this.src1Register = Instruction[2];
                this.src2Register = Instruction[3];
            }
            this.instructionString = instruction;
            this.pc_value = pc_value;
            return this;
        } else {
            return null;
        }

    }
    
    @Override
	public String toString() {
		// TODO Auto-generated method stub
		return instructionString;
	}
}


