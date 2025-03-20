import re
import os
def remove_white_space(asmfile):
     lines = []  # List to store cleaned lines
     new_lines = []
     with open(asmfile, "r") as file:
          for line in file:
               if line[:2] != '//':
                    clean_line = "".join(line.split())  # Remove all whitespace
                    if clean_line:  # Add only if not empty
                         lines.append(clean_line)
          
          for line in lines:
               if line[:2] != '//':
                    new_lines.append(line)
     return new_lines  # Return the list of cleaned lines
def handle_labels(instructions):
     i=0
     symbol_table = {}
     for instruction in instructions:
          open_idx = instruction.find("(")
          close_idx = instruction.find(")")
          if open_idx != -1 and close_idx != -1 and open_idx < close_idx:
               print(instruction)
               symbol_table.update({instruction[open_idx+1:close_idx]: i})     
          else:
               i += 1
               print("pass")
     return symbol_table
def instruction_type(instruction):
     print(f"instruction: {instruction}")
     if instruction[0] == '(':
          return 'L'
     elif instruction[0] == '@':
          return 'A'
     else:
          return 'C'
def handle_comp_instruction(computation):
     comp_str = get_comp_substring(computation)
     dest_str = computation.split("=")[0]
     dest = calculate_dest_bits(dest_str)
     a,comp = calculate_comp_bits(comp_str)
     jump_str = get_jump_substring(computation)
     jump = calculate_jump_bits(jump_str)
     instruction = '111'+ a + comp + dest + jump
     return instruction

def calculate_comp_bits(comp):
     a0_instructions = {'0': "101010", '1': "111111", '-1': '111010', 'D':"001100", 'A': '110000', 
                        '!D':'001101', '!A': '110001', '-D':'001111', '-A': '110011',
                        'D+1':'011111', 'A+1':'110111', 'D-1':'001110', 'A-1': '110010',
                        'D+A': '000010', 'D-A':'010011', 'A-D':'000111', 'D&A':'00000',
                        'D|A': '010101'}
     a1_instructions = {'M': '110000', '!M':'110001', '-M':'110011', 'M+1':'110111', 
                        'M-1': '110010', 'D+M':'000010', 'D-M':'010011', 'M-D': '000111', 
                        'D&M': '000000', 'D|M': '010101'}
     print(f'Comp: {comp}')
     a = ''
     comp_bits = ''
     if comp in a0_instructions:
          a = '0'
          comp_bits = a0_instructions.get(comp)
     elif comp in a1_instructions:
          a= '1'
          comp_bits = a1_instructions.get(comp)
     return a,comp_bits
def get_comp_substring(comp):
     if "=" in comp:
          comp = comp.split("=", 1)[1]  # Get part after '='
     #elif '=' not in comp:  # Check if '=' is not in comp
     #     return 'null'
     if ";" in comp:
          comp = comp.split(";", 1)[0]  # Get part before ';'
     return comp
def get_jump_substring(jump):
     if ';' in jump:
          jump = jump.split(';',1)[1]
     else:
          jump = 'null'
     return jump
def calculate_dest_bits(dest):
     print(f"Dest: {dest}")
     dest_dict = {'null':'000', 'M':'001', 'D':'010', 'MD':'011', 'A': '100', 'AM': '101', 'AD':'110', 'AMD':'111'}
     if dest in dest_dict:
          return dest_dict.get(dest)
     return "000"
def calculate_jump_bits(jump):
     jump_dict = {'null': '000', 'JGT':'001', 'JEQ':'010', 'JGE':'011', 'JLT':'100', 'JNE':'101', 'JLE':'110', 'JMP':'111'}
     if jump in jump_dict:
          return jump_dict.get(jump)
     return ""
        
def assemble_code(asmfile):
     instructions = remove_white_space(asmfile)
     binary_result = parse_instructions(instructions)
     filename = format_file_name(asmfile)
     write_machine_code(filename, binary_result)
     return binary_result

def format_file_name(asmfile):
     filename = os.path.splitext(os.path.basename(asmfile))[0]
     return filename + '.hack'
def write_machine_code(filename, machine_code):
     with open(filename, "w", newline='\n') as file:
          for line in machine_code:
               file.write(str(line) + "\n")  # Convert to string and add newline

def parse_instructions(instructions): 
     MEM = 16
     machine_code = []
     symbol_table ={
    'R0': '0', 'R1': '1', 'R2': '2', 'R3': '3', 'R4': '4', 
    'R5': '5', 'R6': '6', 'R7': '7', 'R8': '8', 'R9': '9', 
    'R10': '10', 'R11': '11', 'R12': '12', 'R13': '13', 
    'R14': '14', 'R15': '15', 'SP':'0', 'LCL':'1', 'ARG':'2', 
    'THIS': '3', 'THAT':'4', 'SCREEN':'16384', 'KBD':'24576'}
     symbol_table.update(handle_labels(instructions))
     for instruction in instructions:
          type = instruction_type(instruction)
          if (type == 'A') and (not instruction[1:].isdigit()):
               if(not (instruction[1:] in symbol_table)):
                    print(f"adding new symbol: {instruction[1:]}")
                    symbol_table.update({instruction[1:] : str(MEM)})
                    value = symbol_table.get(instruction[1:])
                    machine_code.append(bin(int(value))[2:].zfill(16))
                    MEM += 1
               else:
                    value = symbol_table.get(instruction[1:])
                    print(value)
                    machine_code.append(bin(int(value))[2:].zfill(16))         
          elif (type == 'A') and (instruction[1:].isdigit()):
               print(instruction[1:])
               print(bin(int(instruction[1:])))
               machine_code.append(bin(int(instruction[1:]))[2:].zfill(16))
          elif type == 'L':
               continue
          elif type == 'C':
               machine_code.append(handle_comp_instruction(instruction))
     print("Here is the symbol_table: ")
     print(symbol_table)
     return machine_code

def main():
     directory = r'C:\Users\carso\Documents\MachineLearningHw\Group_Project\Assembler\06'
     for file in os.listdir(directory):
          print(f"starting {file}")
          file_path = os.path.join(directory, file)  # Combine directory and filename
          print(assemble_code(file_path))
     #asmfile = r'C:\Users\carso\Documents\MachineLearningHw\Group_Project\Assembler\add.txt'
     
main()

          
          
               

               
               
               
          

          
