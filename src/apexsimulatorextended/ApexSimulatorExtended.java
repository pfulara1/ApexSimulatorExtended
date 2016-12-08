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
import java.util.Queue;
import java.util.Scanner;
import java.util.Queue;

/**
 *
 * @author Paritosh Fulara
 */
public class ApexSimulatorExtended {

    /**
     * @param args the command line arguments
     */
    public static int ZeroFlag = 0;
    public static boolean HALTFLAG = false;
    public static int programCounter = 4000;
    public static HashMap Instruction;
    public static HashMap<Integer, String> InstructionMap = new HashMap<Integer, String>();
    public static LinkedHashMap<String, Integer> registerFile = new LinkedHashMap<String, Integer>();
    public static LinkedHashMap<String, Integer> unifiedRegisterFile = new LinkedHashMap<String, Integer>();
    public static LinkedHashMap<String, RenameTable> renameTable = new LinkedHashMap<String, RenameTable>();
    public static int memory[] = new int[4000];
    public static Scanner sc=new Scanner(System.in);
    public static int sizeUrf=32;
    public static List<Instructions> issueQueue = new ArrayList<Instructions>();
    public static CircularQueue ROB = new CircularQueue();
    public static HashMap<String,Instructions> pipeline= new HashMap<String ,Instructions>();
    public static boolean BranchTaken=false;
    public static boolean isStall=false;
    public static int BranchPcValue;
    public static int NextInstructionBAL;
    public static Queue allocationList=new LinkedList();
    /*
    This function take the file path as a argument read the file line by line 
    and put the instructions in an Hash Map 
   
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
     * @param args the command line arguments
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
                    //Initialize Register and Memory
                    Initialize(args[0]);
                    break;

                case 2:
                    //Simulate the Instruction
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
    
    public  static void Simulate()
    {
        
     System.out.println("Please enter the total number of cycles you want to simulate:");
     Scanner sc = new Scanner(System.in);
     int cycles = sc.nextInt();
     for (int i = 1; i <= cycles; i++) {
            if (HALTFLAG == true) {
                break;
            }    
   // WriteBackALU();
    //WriteBackLSFU();
    //WriteBackMUL();
    //Commit();
    //ExecuteMul();
    //ExecuteLSFU1();
    //ExecuteLSFU2();
    //ExecuteAlu1();
    //ExecuteAlu2();
    //Branch();
     Decode2();
     Decode1();
     FetchStage();
      
    }
    }
        
    public static void FetchStage() {
        if (BranchTaken == true) {
            BranchTaken = false;
        }
        else if (isStall == false && HALTFLAG==false) {
            
                String ins = InstructionMap.get(programCounter);
                Instructions instruction = new Instructions();
                instruction = instruction.ProcessInstruction(ins);
                if (instruction != null) {
                    if (instruction.opcode.equals("BNZ") || instruction.opcode.equals("BZ")) {
                        BranchPcValue = programCounter;
                    }
                    if (instruction.opcode.equals("BAL")) {
                        NextInstructionBAL = programCounter + 4;
                    }
                    pipeline.put("Fetch", instruction);
                    programCounter = programCounter + 4;
                }
            }
        }
    
    public static void Decode1() {
    if(pipeline.get("Fetch")!=null)
    {
     pipeline.put("Decode1",pipeline.get("Fetch"));
    }
   }
    public static void Decode2() {
    if(pipeline.get("Decode1")!=null)
    {
          
     if (BranchTaken == false && HALTFLAG==false) {

            if (isStall == false) {
                   Instructions ins=pipeline.get("Decode1"); 
                    pipeline.put("Decode2", ins);
                    issueQueue.add(ins);
                   switch (ins.opcode) {
                    case "ADD":
                  
                        break;
                    case "SUB":
                       
                        break;
                    case "MUL":
                     
                        break;
                    case "AND":
                       
                        break;
                    case "OR":
                     
                        break;
                    case "EX-OR":
                      
                        break;
                    case "MOVC":
                      // ins.
                        break;
                    case "LOAD":
                    
                        break;
                    case "STORE":
                        break;
                    case "BZ":
                 
                         break;
                    case "BNZ":
                           
                            break;
                    case "JUMP":
                           
                            break;
                    case "BAL":
                         
                       break;
                     
                   
                }     

            }

        }
     
    
   
     
    }
   }
    
    
    
    
    public static void changeUrfSize(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the URF  size");
        sizeUrf = sc.nextInt();
        System.out.println("New URF  size set: "+sizeUrf);
    }
    public static void Initialize(String path) throws IOException {
        ReadInstructionfile(path);
        if(sizeUrf>=32)
        {
            for(int i=0;i<16;i++)
            {
               registerFile.put("R"+i, 0);
            }
            
            for(int i=16;i<sizeUrf;i++)
            {
                registerFile.put("P"+i, 0);
                allocationList.add("P"+i);
            }
            
            registerFile.put("X",0);
          
            for(int k=0;k<4000;k++ )
            {
              memory[k]=0;
            }
            
            for(int i=16;i<sizeUrf;i++)
            {
                registerFile.put("P"+i, 0);
               
            }
            
          
            
            
            
           
            initializePipeline();
            System.out.println("All Initialization Complete\n");
        }
        else
            System.out.println("Initialization cannot be done, URF size should be greater than default size:32");
        
    }
    
    public  static void initializePipeline(){
     
     pipeline.put("Fetch", null);
     pipeline.put("Decode1", null);
     pipeline.put("Decode2", null);
     pipeline.put("Execute1", null);
     pipeline.put("Execute2", null);
     pipeline.put("Multiply", null);
     pipeline.put("LSFU1", null);
     pipeline.put("LSFU2", null);
     pipeline.put("WriteAlu", null);
     pipeline.put("WriteMultiply", null);
     pipeline.put("WriteLSFU", null); 
     pipeline.put("WriteBranch", null);
    
    }
}
    
