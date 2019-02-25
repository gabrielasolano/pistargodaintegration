/**
 *
 * Autor: Daemonio (Marcos Paulo Ferreira)
 * Contato: undefinido gmail com
 * Homepage: https://daemoniolabs.wordpress.com
 *
 * Mon Jul  4 14:44:14 BRT 2011
 *
 */

package br.unb.cic.goda.rtgoretoprism.paramformula;

public class GenerateCombination {
    private int r ;
    private String[] input ;
    private int MAX ;
    private int N ;
 
    //Combination of r elements. If r is zero, then combines every element.
    public GenerateCombination(String[] entrada, int r) {
        this.r = r ;
        this.input = entrada ;
        this.MAX = ~(1 << entrada.length) ;
        this.N = 1;
    }
 
    //True if there is a combination.
    public boolean hasNext() {
        if ( r != 0 ) {
            while ( ((this.N & this.MAX) != 0) && (countbits() != r) ) N+=1 ;
        }
 
        return (this.N & this.MAX) != 0;
    }
 
    private int countbits() {
        int i;
        int c;
 
        i = 1;
        c = 0;
        while ( (this.MAX & i) != 0 ) {
            if ( (this.N & i) != 0) {
                c++;
            }
            i = i << 1 ;
        }
        return c ;
    }
 
    public int getOutputLength() {
        if (r != 0) {
            return r;
        }
 
        return this.countbits();
    }
    
    //Returns a combination.
    public String[] next() {
        int output_index, input_index, i;
 
        String[] output = new String[this.getOutputLength()];
 
        input_index = 0;
        output_index = 0;
        i = 1;
 
        while ((this.MAX & i) != 0) {
            if ((this.N & i) != 0) {
                output[output_index] = input[input_index];
                output_index += 1;
            }
            input_index += 1;
            i = i << 1;
        }
 
        N += 1;
 
        return output;
    }
}
