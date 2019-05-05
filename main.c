#include <stdio.h>
#include <stdlib.h>
#include "Normal.h"
#include "lcgrand.h"
#include <math.h>

FILE *infile, *outfile, *outfile_1;
float generado_normal_1(){

    float w;
    float u1 =lcgrand(1);
    float u2 =lcgrand(1);

    float v1= 2*u1-1;
    float v2= 2*u2-1;
    fprintf(outfile_1, "%f\n",v1);
    w= pow(v1,2)+ pow(v2,2);
    while(w>1){

        u1 =u2;
        u2 =lcgrand(1);
       // printf("%f",u1);
        //printf("     ");
        //printf("%f",u2);
        //printf("\n");
        v1= 2*u1-1;
        v2= 2*u2-1;
        w= pow(v1,2)+ pow(v2,2);
        w = pow(v1,2)+ pow(v2,2);

    }

    float y = sqrt((-2*log(w))/w);
    return v1*y;
};

int main(){


    outfile = fopen("mm1.txt", "w");
    outfile_1 = fopen("mm2.txt", "w");


    for(int i =0 ; i<100; i++){
        float normal = 15 + (3*generado_normal_1());
         fprintf(outfile, "%f\n",normal);

    }
    fclose(outfile_1);
    fclose(outfile);
    return 0;
}
