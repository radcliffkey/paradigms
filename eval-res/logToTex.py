import sys

def getResults(line):
    lidx = line.find('=')
    ridx = line.find(',', lidx)
    precStr =  line[lidx  + 1: ridx]
    prec = float(precStr)
    
    lidx = line.find('=', ridx)
    ridx = line.find(',', lidx)
    recallStr =  line[lidx  + 1: ridx]
    recall = float(recallStr)
    
    lidx = line.find(':', ridx)
    ridx = line.find(']', lidx)
    fmStr =  line[lidx  + 1: ridx]
    fm = float(fmStr)
    
    return prec, recall, fm

def toLatex(table):
    result = """\\begin{table}[h]
\\begin{center}
\\begin{tabular}{lcccccc}
\\toprule
\\bf Experiment & \\bf P-C & \\bf R-C & \\bf F1-C & \\bf P-P & \\bf R-P & \\bf F1-P\\\\
\\midrule
"""
    for row in table:
        result += " & ".join([row[0]] + ["%.2f" % (val * 100)  for val in row[1: ]])
        result += "\\\\\n"
        
    result += """\\bottomrule
\\end{tabular}
\\end{center}
\\caption{\\label{table:}  }
\\end{table}
"""
    return result

if __name__ == '__main__':    
    
    fileNames = sys.argv[1:]
    
    tableData = []
    
    for fileName in fileNames:
        f = open(fileName, 'r')
        lines = f.read().split('\n')
        f.close()
        
        for line in lines:
            if line.startswith('Cluster matching evaluation:'):
                cprec, crecall, cfm = getResults(line)
            if line.startswith('Graph evaluation:'):
                gprec, grecall, gfm = getResults(line)
        
        name = fileName[:fileName.rfind('.')]
        
        tableData.append([name, cprec, crecall, cfm, gprec, grecall, gfm])
    
    print toLatex(tableData)
                
        