
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "lcgrand.h"
float generado_normal(){

    float w;
    float u1 =lcgrand(1);
    float u2 =lcgrand(1);
    float v1= 2*u1-1;
    float v2= 2*u2-1;
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
