 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apexsimulatorextended;

import common.CircularQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Scanner;
import java.util.Queue;

/**
 * 
 * @author Paritosh Fulara
 */
public class ApexSimulatorExtended {

	/**
	 * @param args
	 *            the command line arguments
	 */
	final static String fetch = "fetch";
	final static String decode1 = "decode1";
	final static String decode2 = "decode2";
	final static String execute1 = "execute1", execute2 = "execute2";
	final static String multiply = "multiply", LSFU1 = "LSFU1", LSFU2 = "LSFU2";
	final static String branchALU1 = "branchALU1";
	final static String writeALU = "writeALU", writeMultiply = "writeMultiply",
	writeLSFU = "writeLSFU", writeBranch = "writeBranch";
	public static int ZeroFlag = 0;
	public static boolean HALTFLAG = false;
	public static int programCounter = 4000;
	public static HashMap Instruction;
	public static HashMap<Integer, String> InstructionMap = new HashMap<Integer, String>();
	public static LinkedHashMap<String, Integer> unifiedRegisterFile = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, RenameTable> renameTable = new LinkedHashMap<String, RenameTable>();
	public static int memory[] = new int[4000];
	public static Scanner sc = new Scanner(System.in);
	public static int sizeUrf = 32;
	public static List<IQ> issueQueue = new ArrayList<IQ>(12);
	public static CircularQueue ROB = new CircularQueue(16);
	public static HashMap<String, Instructions> pipeline = new HashMap<String, Instructions>();
	public static boolean BranchTaken = false;
	public static boolean isStall = false;
	public static int BranchPcValue;
	public static int NextInstructionBAL;
	public static Queue allocationList = new LinkedList();
	public static final int ALU = 1;
	public static final int MUL = 2;
	public static final int LSFU = 3;
	public static final int Branch = 4;
	static int index = 0;
	public static int source1, source2;
	public static int literal_zero=0000;
	public static int source1ALU, source2ALU,source1MUL,source2MUL,source1Branch,source2Branch,source1LSFU,source2LSFU;
	/*
	 * This function take the file path as a argument read the file line by line
	 * and put the instructions in an Hash Map
	 */
	public static void ReadInstructionfile(String path) throws IOException {

		FileReader fr = new FileReader(path);
		BufferedReader reader = new BufferedReader(fr);
		int i = 0;
		int instructionCount = 0;
		int pc = programCounter;
		while (reader.readLine() != null) {
			instructionCount++;
		}
		fr = new FileReader(path);
		reader = new BufferedReader(fr);
		for (i = 0; i < instructionCount; i++) {
			InstructionMap.put(pc, reader.readLine());
			pc = pc + 4;
		}
		reader.close();

	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		do {
			System.out.print("Press 1 to Initialize:\n");
			System.out.print("Press 2 Simulate(n) Intruction:\n");
			System.out.print("Press 3 Display:\n");
			System.out.print("Press 4 Set_URF_Size\n");
			System.out.print("Press 5 Exit:\n");

			System.out.print("Please Enter your choice:\n\n");

			int choice = sc.nextInt();

			switch (choice) {
			case 1:
				// Initialize Register and Memory
				Initialize(args[0]);
				break;

			case 2:
				// Simulate the Instruction
				Simulate();
				break;

			case 3:
				// Display the register and memory contents
				// Display();
				break;

			case 4:
				changeUrfSize();
				break;
			case 5:
				System.exit(1);
				break;
			}
		} while (true);

	}

	public static void Simulate() {

		System.out.println("Please enter the total number of cycles you want to simulate:");
		Scanner sc = new Scanner(System.in);
		int cycles = sc.nextInt();
		for (int i = 1; i <= cycles; i++) {
			if (HALTFLAG == true) {
				break;
			}
			// WriteBackALU();
			// WriteBackLSFU();
			// WriteBackMUL();
			 Commit();
			// ExecuteMul();
			// ExecuteLSFU2();
			// ExecuteLSFU1();
			 ExecuteAlu2();
			 ExecuteAlu1();
			// Branch();
			 Decode2();
			 Decode1();
			 FetchStage();

		}
	}


	public static void FetchStage() {
		if (BranchTaken == true) {
			BranchTaken = false;
		} else if (isStall == false && HALTFLAG == false) {

			String ins = InstructionMap.get(programCounter);
			Instructions instruction = new Instructions();
			instruction = instruction.ProcessInstruction(ins);
			if (instruction != null) {
				if (instruction.opcode.equals("BNZ")
						|| instruction.opcode.equals("BZ")) {
					BranchPcValue = programCounter;
				}
				if (instruction.opcode.equals("BAL")) {
					NextInstructionBAL = programCounter + 4;
				}
				pipeline.put(fetch, instruction);
				programCounter = programCounter + 4;
			}
		}
	}

	public static void Decode1() {
		if (BranchTaken == false && HALTFLAG == false && isStall == false) {

			if (pipeline.get(fetch) != null) {
				pipeline.put(decode1, pipeline.get(fetch));
				pipeline.put(fetch, null);

			}
		}
	}

	public static void Decode2() {

 		if (pipeline.get(decode1) != null) {

			RenameTable rt = new RenameTable();
			if (BranchTaken == false && HALTFLAG == false) {

				if (isStall == false) {
					Instructions ins = pipeline.get(decode1);
					IQ issue = new IQ();
					ROB rob = new ROB();

					switch (ins.opcode) {
					case "ADD":
						if (!allocationList.isEmpty()
								&& issueQueue.size() != 12 && ROB.size() != 40) {
							rt.physicalRegister = allocationList.poll()
									.toString();
							rt.valid = false;
							renameTable.put(ins.destRegister, rt);
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;
							// issue queue processing
							issue.fuType = 1;
							issue.destination = rt.physicalRegister;
							issue.ins.physicalDestRegister = rt.physicalRegister;
							issue.ins = ins;
							issueQueue.add(issue);

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = getKeyByValue(InstructionMap,
									ins.instructionString);
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "SUB":
						if (!allocationList.isEmpty()
								&& issueQueue.size() != 12 && ROB.size() != 40) {
							rt.physicalRegister = allocationList.poll()
									.toString();
							rt.valid = false;
							renameTable.put(ins.destRegister, rt);
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;
							// issue queue processing
							issue.fuType = 1;
							issue.destination = rt.physicalRegister;
							issue.ins = ins;
							issueQueue.add(issue);

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = getKeyByValue(InstructionMap,
									ins.instructionString);
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "MUL":
						if (!allocationList.isEmpty()
								&& issueQueue.size() != 12 && ROB.size() != 40) {
							rt.physicalRegister = allocationList.poll()
									.toString();
							rt.valid = false;
							renameTable.put(ins.destRegister, rt);
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;
							// issue queue processing
							issue.fuType = 1;
							issue.destination = rt.physicalRegister;
							issue.ins = ins;
							issueQueue.add(issue);

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = getKeyByValue(InstructionMap,
									ins.instructionString);
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "AND":
						if (!allocationList.isEmpty()
								&& issueQueue.size() != 12 && ROB.size() != 40) {
							rt.physicalRegister = allocationList.poll()
									.toString();
							rt.valid = false;
							renameTable.put(ins.destRegister, rt);
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;
							// issue queue processing
							issue.fuType = 1;
							issue.destination = rt.physicalRegister;
							issue.ins = ins;
							issueQueue.add(issue);

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = getKeyByValue(InstructionMap,
									ins.instructionString);
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "OR":
						if (!allocationList.isEmpty()
								&& issueQueue.size() != 12 && ROB.size() != 40) {
							rt.physicalRegister = allocationList.poll()
									.toString();
							rt.valid = false;
							renameTable.put(ins.destRegister, rt);
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;
							// issue queue processing
							issue.fuType = 1;
							issue.destination = rt.physicalRegister;
							issue.ins = ins;
							issueQueue.add(issue);

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = getKeyByValue(InstructionMap,
									ins.instructionString);
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "EX-OR":
						if (!allocationList.isEmpty()
								&& issueQueue.size() != 12 && ROB.size() != 40) {
							rt.physicalRegister = allocationList.poll()
									.toString();
							rt.valid = false;
							renameTable.put(ins.destRegister, rt);
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;
							// issue queue processing
							issue.fuType = 1;
							issue.destination = rt.physicalRegister;
							issue.ins = ins;
							issueQueue.add(issue);

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = getKeyByValue(InstructionMap,
									ins.instructionString);
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "MOVC":
						// Renaming Logic
						rt.physicalRegister = allocationList.poll().toString();
						rt.valid = false;
						renameTable.put(ins.destRegister, rt);

						// issue queue processing
						issue.fuType = 1;
						issue.destination = rt.physicalRegister;
						issue.ins = ins;
						issue.literal=ins.literal;
						issueQueue.add(issue);

						// ROB processing
						rob.destinationRegsiter = ins.destRegister;
						rob.isValid = false;
						rob.pc = getKeyByValue(InstructionMap, ins.instructionString);
						rob.isValid = false;
						rob.physicalRegister = rt.physicalRegister;
						ROB.add(rob);

						break;
					case "LOAD":
						if (!allocationList.isEmpty()
								&& issueQueue.size() != 12 && ROB.size() != 40) {
							rt.physicalRegister = allocationList.poll()
									.toString();
							rt.valid = false;
							renameTable.put(ins.destRegister, rt);
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}

							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;

							// issue queue processing
							issue.fuType = 3;
							issue.destination = rt.physicalRegister;
							issue.ins = ins;
							issue.literal=ins.literal;
							issueQueue.add(issue);

							// ROB processing
							rob.destinationRegsiter = ins.destRegister;
							rob.isValid = false;
							rob.pc = getKeyByValue(InstructionMap,
									ins.instructionString);
							rob.isValid = false;
							rob.physicalRegister = rt.physicalRegister;
							ROB.add(rob);
						} else {
							isStall = true;
						}
						break;
					case "STORE":
						if (issueQueue.size() != 12) {
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							if (renameTable.get(ins.src2Register).valid == false)
								issue.src2Valid = false;
							else {
								issue.src2Valid = true;
								issue.valuesrc2 = unifiedRegisterFile
										.get(renameTable.get(ins.src2Register).physicalRegister);
							}
							issue.src1Tag = renameTable.get(ins.src1Register).physicalRegister;
							issue.src2Tag = renameTable.get(ins.src2Register).physicalRegister;

							// issue queue processing
							issue.fuType = 3;
							issue.destination = rt.physicalRegister;
							issue.ins = ins;
							issue.literal=ins.literal;
							issueQueue.add(issue);
						} else {
							isStall = true;
						}
						break;
					case "BZ":
						if (issueQueue.size() != 12) {

							// issue queue processing
							issue.fuType = 4;
							issue.ins = ins;
							issueQueue.add(issue);
						} else {
							isStall = true;
						}
						break;
					case "BNZ":
						if (issueQueue.size() != 12) {

							// issue queue processing
							issue.fuType = 4;
							issue.ins = ins;
							issueQueue.add(issue);
						} else {
							isStall = true;
						}
						break;
					case "JUMP":
						if (issueQueue.size() != 12) {

							// issue queue processing
							issue.fuType = 4;
							issue.ins = ins;
							issueQueue.add(issue);
						} else {
							isStall = true;
						}
						break;
					case "BAL":
						if (issueQueue.size() != 12) {
							if (renameTable.get(ins.src1Register).valid == false)
								issue.src1Valid = false;
							else {
								issue.src1Valid = true;
								issue.valuesrc1 = unifiedRegisterFile
										.get(renameTable.get(ins.src1Register).physicalRegister);
							}
							// issue queue processing
							issue.fuType = 4;
							issue.ins = ins;
							issueQueue.add(issue);
						} else {
							isStall = true;
						}
						break;
					case "HALT":
						if (issueQueue.size() != 12) {

							// issue queue processing
							issue.fuType = 4;
							issue.ins = ins;
							issueQueue.add(issue);
						} else {
							isStall = true;
						}
						break;

					}
					
					pipeline.put(decode2, ins);
					pipeline.put(decode1, null);

				}

			}

		}
	}

	public static void ExecuteAlu1() {

		if (!issueQueue.isEmpty()) {
			IQ iq;
			index = issueQueue.size();
			for (int i = 0; i < index; i++) {
				iq = issueQueue.get(index - 1);
				if (iq.fuType == 1 && !iq.ins.opcode.equals("MOVC") && iq.src1Valid == true && iq.src2Valid == true) {
					switch (iq.ins.opcode) {
					case "ADD":
					case "SUB":
					case "AND":
					case "OR":
					case "EX-OR":
						source1=iq.valuesrc1;
						source2=iq.valuesrc2;
						pipeline.put(execute1, iq.ins);
						issueQueue.remove(i);
						break;
					}
					break;
				} 
				else if (iq.fuType == 1 && iq.ins.opcode.equals("MOVC")) {
				source1=iq.literal;
				pipeline.put(execute1, iq.ins);
				issueQueue.remove(i);
				break;
				}
				else 
				{
					index--;
				}
				
			}
		}
	}

	public static void ExecuteAlu2() {
		if(pipeline.get(execute1)!=null){
			Instructions ins=pipeline.get(execute1);
			
			String opcode=ins.opcode;
			int result;
			switch(opcode){
			case "ADD":
				result=source1ALU+source2ALU;
				ins.result=result;
				updateIQ(result,ins);
				updateROB(result, ins);
				break;
			case "SUB":
				result=source1ALU-source2ALU;
				ins.result=result;
				updateIQ(result,ins);
				updateROB(result, ins);
				break;
			case "AND":
				result=source1ALU&source2ALU;
				ins.result=result;
				updateIQ(result,ins);
				updateROB(result, ins);
				break;
			case "OR":
				result=source1ALU|source2ALU;
				ins.result=result;
				updateIQ(result,ins);
				updateROB(result, ins);
				break;
			case "EX-OR":
				result=source1ALU^source2ALU;
				ins.result=result;
				updateIQ(result,ins);
				updateROB(result, ins);
				break;
			case "MOVC":
				result=literal_zero+source1ALU;
				ins.result=result;
				updateIQ(result, ins);
				updateROB(result, ins);
				break;
			}
			
			pipeline.put(execute2, ins);
			pipeline.put(execute1, null);
					
					
		}
	}
	
	public static void Commit() {
		
		ROB rob_array[] = null;
		rob_array = ROB.getQ();
		ROB rob = new ROB();
		rob = rob_array[ROB.getHeadIndex()];
		if(rob != null && rob.isValid)
		{
			unifiedRegisterFile.put(rob.destinationRegsiter, rob.value);
		}
		
	}
	
	static void updateIQ(int result, Instructions ins){
		for(int i=0;i<issueQueue.size();i++){
			Instructions IQins=issueQueue.get(i).ins;
			if(IQins.opcode.equals("MOVC")&&IQins.src1Register.equals(ins.destRegister)){
				issueQueue.get(i).valuesrc1=result;
				issueQueue.get(i).src1Valid=true;
				break;
			}
			if(IQins.src1Register.equalsIgnoreCase(ins.destRegister)){
				issueQueue.get(i).valuesrc1=result;
				issueQueue.get(i).src1Valid=true;
			}
			if(IQins.src2Register.equalsIgnoreCase(ins.destRegister)){
				issueQueue.get(i).valuesrc2=result;
				issueQueue.get(i).src2Valid=true;
				break;
			}
		}
	}
	
	static void updateROB(int result, Instructions ins){
		ROB rob_array[] = null;
		rob_array = ROB.getQ();
		int headIndex = ROB.getHeadIndex();
		for(int i=headIndex;i>=0;i--){
			if(rob_array[i].physicalRegister.equalsIgnoreCase(ins.physicalDestRegister)){
				rob_array[i].value=ins.result;
				rob_array[i].isValid=true;
				break;
			}
		}
	}
	static void WriteBackALU(){
		if(pipeline.get(execute2)!=null){
			Instructions ins=pipeline.get(execute2);
			pipeline.put(writeALU, ins);
			unifiedRegisterFile.put(ins.physicalDestRegister, ins.result);
			allocationList.add(ins.physicalDestRegister);
			renameTable.get(ins.physicalDestRegister).valid=true;
			
		}	
	}
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			String temp = (String) entry.getValue();
			temp = temp.replaceAll(",", " ");
			if (Objects.equals(value, temp)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static void changeUrfSize() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the URF  size");
		sizeUrf = sc.nextInt();
		System.out.println("New URF  size set: " + sizeUrf);
	}

	public static void Initialize(String path) throws IOException {
		ReadInstructionfile(path);
		if (sizeUrf >= 32) {
			for (int i = 0; i < 16; i++) {
				unifiedRegisterFile.put("R" + i, 0);
			}

			for (int i = 16; i < sizeUrf; i++) {
				unifiedRegisterFile.put("P" + i, 0);
				allocationList.add("P" + i);
			}

			unifiedRegisterFile.put("X", 0);

			for (int k = 0; k < 4000; k++) {
				memory[k] = 0;
			}

			for (int i = 16; i < sizeUrf; i++) {
				unifiedRegisterFile.put("P" + i, 0);

			}

			initializePipeline();
			System.out.println("All Initialization Complete\n");
		} else
			System.out
					.println("Initialization cannot be done, URF size should be greater than default size:32");

	}

	public static void initializePipeline() {

		pipeline.put(fetch, null);
		pipeline.put(decode1, null);
		pipeline.put(decode2, null);
		pipeline.put(execute1, null);
		pipeline.put(execute2, null);
		pipeline.put(multiply, null);
		pipeline.put(LSFU1, null);
		pipeline.put(LSFU2, null);
		pipeline.put(branchALU1, null);
		pipeline.put(writeALU, null);
		pipeline.put(writeMultiply, null);
		pipeline.put(writeLSFU, null);
		pipeline.put(writeBranch, null);

	}
}                        
