## For testing:
Note that because this mini-project requires multiple MPI processes to be spawned, performing local testing using Maven as you normally would will only result in a single MPI rank executing. To execute a local MPI program with 4 MPI ranks run the following command:
```bash
	$ mvn -P MPITests-4 test-compile
```