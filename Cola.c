#include <stdio.h>}
#include <stdlib.h>
struct objeto {
  int id;
    struct objeto *anterior;
  struct objeto *siguiente;
};

typedef struct objeto Nodo;
int size;
Nodo *fina;
Nodo *inicio;

void inicializar (){
 fina = inicio = 0;

};
int is_empty() {
  if (!fina) {
    return 1;
  } else {
    return 0;
  }
}

void queue (int id){
   Nodo *nodoNuevo,
       *temporal;

  nodoNuevo = (Nodo*) malloc(sizeof(Nodo));


  nodoNuevo->id = id;
  if (is_empty()) {
    fina = nodoNuevo;
    inicio = nodoNuevo;
  } else {
    temporal = fina;
    fina = nodoNuevo;
    temporal->siguiente = fina;
    fina->anterior = temporal;
  }
};
int dequeue (int id){
   Nodo *temporal, * actual;

  if (!is_empty()) {
        if (fina == inicio) {
      actual= fina;
       //printf("%d ", fina->id);


      fina = inicio = 0;
      return actual->id;
    } else {

      actual = fina;
     // printf("%d ", fina->id);

     fina= fina->anterior;
     return actual->id;
    }
  }

};
int main()
{
    inicializar();
    for (int i =0; i<10; i++){
        queue(i);
    };
    for (int i =0; i<10; i++){

        printf("%d ", dequeue(i));
        printf("\n");
    };

    return 0;
}

