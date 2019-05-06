/* External definitions for single-server queueing system. */

#include <stdio.h>
#include <math.h>
#include "lcgrand.h"  /* Header file for random-number generator. */

#define Distancia_cajas 2.1
#define Largo_banda 30
#define Q_LIMIT 100000  /* Limit on queue length. */
#define BUSY      1  /* Mnemonics for server's being busy */
#define IDLE      0  /* and idle. */

int   next_event_type, num_events, num_in_q, server_status, numero_fin_re, numero_ini_re,num_delays_required;
float area_server_status,sim_time, time_last_event;

FILE  *infile, *outfile;

///Creados nuevos
/// TNE      1- ARRIVAL     2-RECOLECCION    3-SERVER_IDLE     4-FIN_RECOLECCION
float mean_descarga,desv_e_descarga,velocidad_cinta,time_next_event[5][20];
int x,y;


void  initialize(void);
void  timing(void);
void  report(void);
void  update_time_avg_stats(void);
float expon(float mean);
void inicio_recoleccion (void);
float generado_normal_1(float mean, float var);
void server_idle(void);
void arrival(void);
void fin_recoleccion(void);



int k;

main()  /* Main function. */
{
    /* Open input and output files. */

    infile  = fopen("mm1.in",  "r");

    outfile = fopen("mm1.out", "w");

    fscanf(infile, "%f %f %f %d", &mean_descarga, &desv_e_descarga,&velocidad_cinta,
           &num_delays_required);

    for(k = 0;k<20;k++){
    fprintf(outfile,"\n------------------------\n");
    fprintf(outfile,"\n\nSimulacion numero %d\n\n",k);
    /* Specify the number of events for the timing function. */

    num_events = 4;

    /* Read input parameters. */

    /* Write report heading and input parameters. */

    fprintf(outfile, "Single-server queueing system\n\n");
    fprintf(outfile, "Tiempo medio de descarga %11.3f minutos\n\n",
            mean_descarga);

    fprintf(outfile, "Desviacion estandar de descarga %16.3f minutos\n\n", desv_e_descarga);
    fprintf(outfile, "Numero de cajas %14d\n\n", num_delays_required);



    /* Initialize the simulation. */

    initialize();

    /* Run the simulation while more delays are still needed. */

    while (x+y <= num_delays_required)///Verifica que no hayan salido 1000 cajas, fin de la mimulacion
    {
        /* Determine the next event. */

        timing();

        /* Update time-average statistical accumulators. */

        update_time_avg_stats();

        /* Invoke the appropriate event function. */

        switch (next_event_type)
        {
            /// TNE      1- ARRIVAL     2-RECOLECCION    3-SERVER_IDLE     4-FIN_RECOLECCION
            case 1:
                arrival();
                break;
            case 2:
                inicio_recoleccion();
                break;
            case 3:
                server_idle();
                break;
            case 4:
                fin_recoleccion();
                break;
        }
    }

    /* Invoke the report generator and end the simulation. */

    report();
    }
    //endfor

    fclose(infile);
    fclose(outfile);

    return 0;
}

void initialize(void)  /* Initialization function. */
{
    /* Initialize the simulation clock. */

    sim_time = 0.0;

    /* Initialize the state variables. */

    server_status   = IDLE;
    num_in_q        = 0;

    /* Initialize the statistical counters. */
    area_server_status = 0.0;


    numero_ini_re = 0;
    numero_fin_re = 0;

    time_last_event = 0.0;

    /* Initialize event list.  Since no customers are present, the departure
       (service completion) event is eliminated from consideration. */

    /// TNE      1- ARRIVAL     2-INICIO_RECOLECCION    3-SERVER_IDLE     4-FIN_RECOLECCION

    time_next_event[1][1] = sim_time + 2.10/velocidad_cinta;
    time_next_event[2][1] = 1.0e+30;
    time_next_event[3][1] = 1.0e+30;
    time_next_event[4][1] = 1.0e+30;
}


void timing(void)  /* Timing function. */
{
    int   i;
    float min_time_next_event = 1.0e+29;

    next_event_type = 0;

    /* Determine the event type of the next event to occur. */

    for (i = 1; i <= num_events; ++i)
        if (time_next_event[i][1] < min_time_next_event)
        {
            min_time_next_event = time_next_event[i][1];
            next_event_type     = i;
        }

        /* Check to see whether the event list is empty. */

    if (next_event_type == 0)
    {
        /* The event list is empty, so stop the simulation. */

        fprintf(outfile, "\nEvent list empty at time %f", sim_time);
        exit(1);
    }

    /* The event list is not empty, so advance the simulation clock. */

    sim_time = min_time_next_event;
}



void report(void)  /* Report generator function. */
{
    /* Compute and write estimates of desired measures of performance. */

    fprintf(outfile, "\n\nAverage delay in queue%11.3f minutes\n\n",
            total_of_delays / num_custs_delayed);
    fprintf(outfile, "Average number in queue%10.3f\n\n",
            area_num_in_q / sim_time);
    fprintf(outfile, "Server utilization%15.3f\n\n",
            area_server_status / sim_time);
    fprintf(outfile, "Time simulation ended%12.3f minutes", sim_time);

}

void arrival(void){
    numero_ini_re++;
    time_next_event[1][1] = sim_time + Distancia_cajas/velocidad_cinta;  // Se programa el siguiente evento de llegada
    time_next_event[2][numero_ini_re]= sim_time + Largo_banda/velocidad_cinta;  // Tiempo de inicio de recolección
}

void inicio_recoleccion (void)
{
    if(server_status==BUSY){

            /**agendar evento fin de recoleccion de la caja*/
        time_next_event[4][++numero_fin_re] = sim_time + (Distancia_cajas/velocidad_cinta);

    }else{
        server_status= BUSY;
        x++;
        for(int i =1 ; i <=numero_ini_re;i++){
            time_next_event[2][i] = time_next_event[4][i+1];

        }
        numero_ini_re--;
        /** se agenda el evento server_idle*/
            time_next_event[3][1]= generado_normal_1(mean_descarga, desv_e_descarga);
    }


}


void server_idle(void){
    server_status = IDLE;
    if(numero_fin_re >= 1){
        time_next_event[4][1] = sim_time;
    }
}

void fin_recoleccion(void){

}


void update_time_avg_stats(void)  /* Update area accumulators for time-average
                                     statistics. */
{
    float time_since_last_event;

    time_since_last_event = sim_time - time_last_event;
    time_last_event = sim_time;

    area_server_status += server_status*time_since_last_event;
}

float generado_normal_1(float mean, float var){

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
        v1= 2*u1-1;
        v2= 2*u2-1;
        w= pow(v1,2)+ pow(v2,2);
        w = pow(v1,2)+ pow(v2,2);

    }

    float y = sqrt((-2*log(w))/w);
    return mean + (var*v1*y);
};
