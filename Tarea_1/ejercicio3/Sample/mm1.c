/* External definitions for single-server queueing system. */

#include <stdio.h>
#include <math.h>
#include "lcgrand.h"  /* Header file for random-number generator. */

#define Q_LIMIT 100000  /* Limit on queue length. */
#define BUSY      1  /* Mnemonics for server's being busy */
#define IDLE      0  /* and idle. */

int   next_event_type, num_custs_delayed_1, num_custs_delayed_2, num_delays_required, num_events,
      num_in_q_1,num_in_q_2, server_status_1,server_status_2;
float area_num_in_q_1,area_num_in_q_2, area_server_status_1,area_server_status_2, mean_interarrival, mean_service_1, mean_service_2,
      sim_time, time_arrival_1[11],time_arrival_2[Q_LIMIT + 1], time_last_event, time_next_event[4],
      total_of_delays_1,total_of_delays_2;
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

    fscanf(infile, "%f %f %f %d", &mean_interarrival, &mean_service_1, &mean_service_2,
           &num_delays_required);



    for(k = 0;k<20;k++){
    fprintf(outfile,"\n------------------------\n");
    fprintf(outfile,"\n\nSimulacion numero  %d\n\n",k);
    /* Specify the number of events for the timing function. */

    num_events = 3;

    /* Read input parameters. */


    /* Write report heading and input parameters. */

    fprintf(outfile, "Single-server queueing system\n\n");
    fprintf(outfile, "Mean interarrival time%11.3f personas\n\n",
            mean_interarrival);
    fprintf(outfile, "Mean service time 1 %16.3f minutes\n\n", mean_service_1);
    fprintf(outfile, "Mean service time 2 %16.3f minutes\n\n", mean_service_2);
    fprintf(outfile, "Number of customers%14d\n\n", num_delays_required);



    /* Initialize the simulation. */

    initialize();

    /* Run the simulation while more delays are still needed. */

    while (num_custs_delayed_1+ num_custs_delayed_2 < num_delays_required)
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

    server_status_1   = IDLE;
    server_status_2   = IDLE;
    num_in_q_1        = 0;
    num_in_q_2        = 0;
    time_last_event = 0.0;

    /* Initialize the statistical counters. */

    num_custs_delayed_1  = 0;
    num_custs_delayed_2  = 0;
    total_of_delays_1    = 0.0;
    total_of_delays_2    = 0.0;
    area_num_in_q_1      = 0.0;
    area_num_in_q_2      = 0.0;
    area_server_status_1 = 0.0;
    area_server_status_2 = 0.0;

    /* Initialize event list.  Since no customers are present, the departure
       (service completion) event is eliminated from consideration. */

    time_next_event[1] = sim_time + poisson(mean_interarrival);
    time_next_event[2] = 1.0e+30;
    time_next_event[3] = 1.0e+30;
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

    time_next_event[1] = sim_time + poisson(mean_interarrival);

    /* Check to see whether server is busy. */

    if (num_in_q_1>=10)
    {
      if(server_status_2==BUSY){
        ++num_in_q_2;

        /* Check to see whether an overflow condition exists. */

        if (num_in_q_2 > Q_LIMIT)
        {
            /* The queue has overflowed, so stop the simulation. */

            fprintf(outfile, "\nOverflow of the array time_arrival at");
            fprintf(outfile, " time %f", sim_time);
            exit(2);
        }

        /* There is still room in the queue, so store the time of arrival of the
           arriving customer at the (new) end of time_arrival. */

        time_arrival_2[num_in_q_2] = sim_time;
    }

    else
    {
        /* Server is idle, so arriving customer has a delay of zero.  (The
           following two statements are for program clarity and do not affect
           the results of the simulation.) */

        delay            = 0.0;
        total_of_delays_2 += delay;

        /* Increment the number of customers delayed, and make server busy. */

        ++num_custs_delayed_2;
        server_status_2 = BUSY;

        /* Schedule a departure (service completion). */

        time_next_event[3] = sim_time + expon(mean_service_2);


      }
    }
    else{
              if(server_status_1==BUSY){
                ++num_in_q_1;

        /* Check to see whether an overflow condition exists. */

           /* There is still room in the queue, so store the time of arrival of the
           arriving customer at the (new) end of time_arrival. */

            time_arrival_1[num_in_q_1] = sim_time;
    }

    else
    {
        /* Server is idle, so arriving customer has a delay of zero.  (The
           following two statements are for program clarity and do not affect
           the results of the simulation.) */

        delay            = 0.0;
        total_of_delays_1 += delay;

        /* Increment the number of customers delayed, and make server busy. */

        ++num_custs_delayed_1;
        server_status_1 = BUSY;

        /* Schedule a departure (service completion). */

        time_next_event[2] = sim_time + expon(mean_service_1);


      }
    }

}
  /* Server is busy, so increment number of customers in queue. */



void depart_1(void)  /* Departure event function. */ ///DEPARTURE SERVIDOR 1  EVENTO TIPO 2
{
    int   i;
    float delay;

    /* Check to see whether the queue is empty. */

    if (num_in_q_1 == 0)
    {
        /* The queue is empty so make the server idle and eliminate the
           departure (service completion) event from consideration. */

        server_status_1      = IDLE;
        time_next_event[2] = 1.0e+30;
    }

    else
    {
        /* The queue is nonempty, so decrement the number of customers in
           queue. */

        --num_in_q_1;

        /* Compute the delay of the customer who is beginning service and update
           the total delay accumulator. */

        delay            = sim_time - time_arrival_1[1];
        total_of_delays_1 += delay;

        /* Increment the number of customers delayed, and schedule departure. */

        ++num_custs_delayed_1;
        time_next_event[2] = sim_time + expon(mean_service_1);

        /* Move each customer in queue (if any) up one place. */

        for (i = 1; i <= num_in_q_1; ++i)
            time_arrival_1[i] = time_arrival_1[i + 1];
    }
}


void depart_2(void)  /* Departure event function. */ ///DEPARTURE SERVIDOR 2  EVENTO TIPO 3
{
    int   i;
    float delay;

    /* Check to see whether the queue is empty. */

    if (num_in_q_2 == 0)
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

        --num_in_q_2;

        /* Compute the delay of the customer who is beginning service and update
           the total delay accumulator. */

        delay            = sim_time - time_arrival_2[1];
        total_of_delays_2 += delay;

        /* Increment the number of customers delayed, and schedule departure. */

        ++num_custs_delayed_2;
        time_next_event[3] = sim_time + expon(mean_service_2);

        /* Move each customer in queue (if any) up one place. */

        for (i = 1; i <= num_in_q_2; ++i)
            time_arrival_2[i] = time_arrival_2[i + 1];
    }
}



void report(void)  /* Report generator function. */
{
    /* Compute and write estimates of desired measures of performance. */

    fprintf(outfile, "\n\nAverage delay in queue 1%11.3f minutes\n\n",
            total_of_delays_1 / num_custs_delayed_1);
    fprintf(outfile, "Average number in queue 1%10.3f\n\n",
            area_num_in_q_1 / sim_time);
    fprintf(outfile, "Server utilization 1%15.3f\n\n",
            area_server_status_1 / sim_time);
    fprintf(outfile, "\n\nAverage delay in queue 2%11.3f minutes\n\n",
            total_of_delays_2 / num_custs_delayed_2);
    fprintf(outfile, "Average number in queue 2%10.3f\n\n",
            area_num_in_q_2 / sim_time);
    fprintf(outfile, "Server utilization 2%15.3f\n\n",
            area_server_status_2 / sim_time);
    fprintf(outfile, "Time simulation ended%12.3f minutes\n\n", sim_time);

}


void update_time_avg_stats(void)  /* Update area accumulators for time-average
                                     statistics. */
{
    float time_since_last_event;

    /* Compute time since last event, and update last-event-time marker. */

    time_since_last_event = sim_time - time_last_event;
    time_last_event       = sim_time;

    /* Update area under number-in-queue function. */

    area_num_in_q_1      += num_in_q_1 * time_since_last_event;
    area_num_in_q_2     += num_in_q_2 * time_since_last_event;

    /* Update area under server-busy indicator function. */

    area_server_status_1 += server_status_1 * time_since_last_event;
    area_server_status_2 += server_status_2 * time_since_last_event;
}


float expon(float mean)  /* Exponential variate generation function. */
{
    /* Return an exponential random variate with mean "mean". */
    return -mean * log(lcgrand(2));
}

float poisson(float beta){
    float lambda = 1/beta;
    return -lambda * log(lcgrand(2));
}
