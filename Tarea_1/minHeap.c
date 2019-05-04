#include <stdio.h>
#include <stdlib.h>

void maxHeapify(int *arr, int index, int size) {
	int child = 2 * index;
	if (child <= size) {
		if (child + 1 <= size && arr[child + 1] < arr[child]) {
			child++;
		}
		if (arr[index] > arr[child]) {
			int temp = arr[index];
			arr[index] = arr[child];
			arr[child] = temp;
			maxHeapify(arr, child, size);
		}
	}
}

int extractMin(int *arr, int* size) {
	int toReturn = arr[1];
	arr[1] = arr[*size];
	arr[*size] = 0;
	*size = *size - 1;
	maxHeapify(arr, 1, *size);
	return toReturn;
}

void insertHeap(int *arr, int* size, int number) {
	*size = *size + 1;
	arr[*size] = number;
	int pos = *size;
	while (pos / 2 >= 1 && arr[pos / 2] > arr[pos]) {
		int temp = arr[pos / 2];
		arr[pos / 2] = arr[pos];
		arr[pos] = temp;
		pos /= 2;
	}
}

//Size es igual al numero de nodos
int main(int argc, char *argv[]) {
	int size = 0;
	int arr[100001];
	for (int i = 200; i >= 101; i--) {
		insertHeap(arr, &size, i);
	}
	for (; size > 0;)
	{
		printf("%i value\n", extractMin(arr, &size));
	}
	return 0;
}