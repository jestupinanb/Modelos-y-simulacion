/* External definitions for single-server queueing system. */

#include <stdio.h>
#include <math.h>
#include "lcgrand.h"  /* Header file for random-number generator. */

#define Q_LIMIT 100  /* Limit on queue length. */
#define BUSY      1  /* Mnemonics for server's being busy */
#define IDLE      0  /* and idle. */

int   next_event_type, num_custs_delayed, num_delays_required, num_events,
      num_in_q, server_status,server_status_2,server_status_3;
float area_num_in_q, area_server_status, mean_interarrival, mean_service,
      sim_time, time_arrival[Q_LIMIT + 1], time_last_event, time_next_event[5],
      total_of_delays, total_of_delays_total;
FILE  *infile, *outfile;

void  initialize(void);
void  timing(void);
void  arrive(void);
void  depart(void);
void  report(void);
void  update_time_avg_stats(void);
float expon(float mean);
float poisson(float beta);

int k;

main()  /* Main function. */
{
    /* Open input and output files. */

    infile  = fopen("mm1.in",  "r");

    outfile = fopen("mm1.out", "w");

     fscanf(infile, "%f %f %d", &mean_interarrival, &mean_service,
           &num_delays_required);

    mean_interarrival =  60/mean_interarrival;

    for(k = 0;k<100;k++){
    //fprintf(outfile,"\n------------------------\n");
    //fprintf(outfile,"\n\nSimulacion numero %d\n\n",k);
    /* Specify the number of events for the timing function. */

    num_events = 3;

    /* Read input parameters. */



    /* Write report heading and input parameters. */

    //fprintf(outfile, "Single-server queueing system\n\n");
    //fprintf(outfile, "Mean interarrival time%11.3f minutes\n\n",
            //mean_interarrival);
    //fprintf(outfile, "Mean service time %16.3f minutes\n\n", mean_service);
    //fprintf(outfile, "Number of customers%14d\n\n", num_delays_required);



    /* Initialize the simulation. */

    initialize();

    /* Run the simulation while more delays are still needed. */

    while (num_custs_delayed < num_delays_required)
    {
        /* Determine the next event. */

        timing();

        /* Update time-average statistical accumulators. */

        update_time_avg_stats();

        /* Invoke the appropriate event function. */

        switch (next_event_type)
        {
            case 1:
                arrive();
                break;
            case 2:
                depart_1();
                break;
            case 3:
                depart_2();
                break;
            case 4:
                depart_3();
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
    server_status_2   = IDLE;
    //server_status_3   = IDLE;
    num_in_q        = 0;
    time_last_event = 0.0;

    /* Initialize the statistical counters. */

    num_custs_delayed  = 0;
    total_of_delays    = 0.0;
    area_num_in_q      = 0.0;
    area_server_status = 0.0;
    total_of_delays_total=0.0;

    /* Initialize event list.  Since no customers are present, the departure
       (service completion) event is eliminated from consideration. */

    time_next_event[1] = sim_time + poisson(mean_interarrival);
    time_next_event[2] = 1.0e+30;
    time_next_event[3] = 1.0e+30;
    //time_next_event[4] = 1.0e+30;
}


void timing(void)  /* Timing function. */
{
    int   i;
    float min_time_next_event = 1.0e+29;

    next_event_type = 0;

    /* Determine the event type of the next event to occur. */

    for (i = 1; i <= num_events; ++i)
        if (time_next_event[i] < min_time_next_event)
        {
            min_time_next_event = time_next_event[i];
            next_event_type     = i;
        }
    printf("\n%d\n", next_event_type);
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


void arrive(void)  /* Arrival event function. */
{
    float delay;

    /* Schedule next arrival. */

    time_next_event[1] = sim_time + expon(mean_interarrival);

    /* Check to see whether server is busy. */

    if (server_status == BUSY)
    {
        // printf("arrive_1 ");
        if(server_status_2==BUSY){
                ++num_in_q;



        if (num_in_q > Q_LIMIT)
        {


            fprintf(outfile, "\nOverflow of the array time_arrival at");
            fprintf(outfile, " time %f", sim_time);
            exit(2);
        }

        time_arrival[num_in_q] = sim_time;
        /* Server is busy, so increment number of customers in queue. */
           /* if(server_status_3 ==BUSY ){
                printf("arrive_4 ");
        ++num_in_q;



        if (num_in_q > Q_LIMIT)
        {


            fprintf(outfile, "\nOverflow of the array time_arrival at");
            fprintf(outfile, " time %f", sim_time);
            exit(2);
        }

        time_arrival[num_in_q] = sim_time;


        }
            else{

                delay            = 0.0;
        total_of_delays += delay;



        ++num_custs_delayed;
        server_status_3 = BUSY;



        time_next_event[4] = sim_time + expon(mean_service);
        printf("\n%f\n", time_next_event[4]);


            }*/
        }

        else{
        delay            = 0.0;
        total_of_delays += delay;

        /* Increment the number of customers delayed, and make server busy. */

        ++num_custs_delayed;
        server_status_2 = BUSY;

        /* Schedule a departure (service completion). */

        time_next_event[3] = sim_time + expon(mean_service);
        //printf("\n%f\n", time_next_event[3]);

        }
    }

    else
    {
        /* Server is idle, so arriving customer has a delay of zero.  (The
           following two statements are for program clarity and do not affect
           the results of the simulation.) */

        delay            = 0.0;
        total_of_delays += delay;

        /* Increment the number of customers delayed, and make server busy. */

        ++num_custs_delayed;
        server_status = BUSY;

        /* Schedule a departure (service completion). */

        time_next_event[2] = sim_time + expon(mean_service);
    }
}

/*
void depart(void)
    int   i;
    float delay;



    if (num_in_q == 0)
    {


        server_status      = IDLE;
        time_next_event[2] = 1.0e+30;
    }

    else
    {


        --num_in_q;



        delay            = sim_time - time_arrival[1];
        total_of_delays += delay;
        total_of_delays_total += delay;


        ++num_custs_delayed;
        float service =expon(mean_service);
        time_next_event[2] = sim_time + service;
        total_of_delays_total += service;


        for (i = 1; i <= num_in_q; ++i)
            time_arrival[i] = time_arrival[i + 1];
    }
}
*/

void depart_1(void)  /* Departure event function. */
{
//printf("departe_1");
    int   i;
    float delay;

    /* Check to see whether the queue is empty. */

    if (num_in_q == 0)
    {
        /* The queue is empty so make the server idle and eliminate the
           departure (service completion) event from consideration. */

        server_status      = IDLE;
        time_next_event[2] = 1.0e+30;
    }

    else
    {
        /* The queue is nonempty, so decrement the number of customers in
           queue. */

        --num_in_q;

        /* Compute the delay of the customer who is beginning service and update
           the total delay accumulator. */

        delay            = sim_time - time_arrival[1];
        total_of_delays += delay;
        total_of_delays_total += delay;
        /* Increment the number of customers delayed, and schedule departure. */

        ++num_custs_delayed;
        float service =expon(mean_service);
        time_next_event[2] = sim_time + service;
        total_of_delays_total += service;
        /* Move each customer in queue (if any) up one place. */

        for (i = 1; i <= num_in_q; ++i)
            time_arrival[i] = time_arrival[i + 1];
    }
}

void depart_2(void)  /* Departure event function. */
{ //printf("departe_2");
    int   i;
    float delay;

    /* Check to see whether the queue is empty. */

    if (num_in_q == 0)
    {
        /* The queue is empty so make the server idle and eliminate the
           departure (service completion) event from consideration. */

        server_status_2      = IDLE;
        time_next_event[3] = 1.0e+30;
    }

    else
    {
        /* The queue is nonempty, so decrement the number of customers in
           queue. */

        --num_in_q;

        /* Compute the delay of the customer who is beginning service and update
           the total delay accumulator. */

        delay            = sim_time - time_arrival[1];
        total_of_delays += delay;
        total_of_delays_total += delay;
        /* Increment the number of customers delayed, and schedule departure. */

        ++num_custs_delayed;
        float service =expon(mean_service);
        time_next_event[3] = sim_time + service;
        total_of_delays_total += service;
        /* Move each customer in queue (if any) up one place. */

        for (i = 1; i <= num_in_q; ++i)
            time_arrival[i] = time_arrival[i + 1];
    }
}


void depart_3(void)  /* Departure event function. */
{
    // printf("departe_3");
    int   i;
    float delay;

    /* Check to see whether the queue is empty. */

    if (num_in_q == 0)
    {
        /* The queue is empty so make the server idle and eliminate the
           departure (service completion) event from consideration. */

        server_status_3      = IDLE;
        time_next_event[4] = 1.0e+30;
    }

    else
    {
        /* The queue is nonempty, so decrement the number of customers in
           queue. */

        --num_in_q;

        /* Compute the delay of the customer who is beginning service and update
           the total delay accumulator. */

        delay            = sim_time - time_arrival[1];
        total_of_delays += delay;
        total_of_delays_total += delay;
        /* Increment the number of customers delayed, and schedule departure. */

        ++num_custs_delayed;
        float service =expon(mean_service);
        time_next_event[4] = sim_time + service;
        total_of_delays_total += service;
        /* Move each customer in queue (if any) up one place. */

        for (i = 1; i <= num_in_q; ++i)
            time_arrival[i] = time_arrival[i + 1];
    }
}
void report(void)  /* Report generator function. */
{
    /* Compute and write estimates of desired measures of performance. */

    fprintf(outfile, "%15.3f\n",
            (total_of_delays / num_custs_delayed)*1000);
    /*
    fprintf(outfile, "\n\nAverage delay in queue%11.3f minutes\n\n",
            total_of_delays / num_custs_delayed);
    fprintf(outfile, "Average number in queue%10.3f\n\n",
            area_num_in_q / sim_time);
    fprintf(outfile, "Server utilization%15.3f\n\n",
            area_server_status / sim_time);
    fprintf(outfile, "Time simulation ended%12.3f minutes", sim_time);
    */
}


void update_time_avg_stats(void)  /* Update area accumulators for time-average
                                     statistics. */
{
    float time_since_last_event;

    /* Compute time since last event, and update last-event-time marker. */

    time_since_last_event = sim_time - time_last_event;
    time_last_event       = sim_time;

    /* Update area under number-in-queue function. */

    area_num_in_q      += num_in_q * time_since_last_event;

    /* Update area under server-busy indicator function. */

    area_server_status += server_status * time_since_last_event;
    area_server_status += server_status_2 * time_since_last_event;
}


float expon(float mean)  /* Exponential variate generation function. */
{
    /* Return an exponential random variate with mean "mean". */
    return -mean * log(lcgrand(2));
}

float poisson(float beta){
    float lambda = beta;
    return -lambda * log(lcgrand(2));
}
