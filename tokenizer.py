import javalang
import sys

def camel_case_split(str): 
    words = [[str[0]]] 
  
    for c in str[1:]: 
        if words[-1][-1].islower() and c.isupper(): 
            words.append(list(c)) 
        else: 
            words[-1].append(c) 
  
    return [''.join(word) for word in words] 

with open('expressions.txt') as f:
    content = f.readlines()
# you may also want to remove whitespace characters like `\n` at the end of each line
# content = [x.strip() for x in content] 

for index in range(0,len(content)):
    expression = content[index]
    # print("EXP " +expression)

    tokens = list(javalang.tokenizer.tokenize(expression))
    # print(tokens)
    output = ""
    for i in range(0,len(tokens)):
        if i!=len(tokens)-1:
            if(str(type(tokens[i]))=="<class 'javalang.tokenizer.Identifier'>"):
                words = camel_case_split(tokens[i].value)
                for j in range(0,len(words)):
                    output = output + words[j].lower()+"\n"
            else:
                output = output + tokens[i].value+"\n"
        else:
            if(str(type(tokens[i]))=="<class 'javalang.tokenizer.Identifier'>"):
                words = camel_case_split(tokens[i].value)
                size = len(words)
                for j in range(0,size-1):
                    output = output + words[j].lower()+"\n"
                output = output + words[size-1].lower()+"\n"
            else:
                output = output + tokens[i].value+"\n"

    print(output)

# for i in range(0,len(content)):
#     tokens = list(javalang.tokenizer.tokenize(content[i]))
#     if(len(tokens)==0):
#         output = output + "empty"+"\n\n"
#     for j in range(0,len(tokens)):
#         if j!=len(tokens)-1:
#             output = output + tokens[j].value+"\n" #+ str(type(tokens[j]))+"\n"
#         else:
#             output = output + tokens[j].value+"\n\n"
        
