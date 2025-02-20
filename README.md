# JuiceBottler
Lab1 Operating systems - JuiceBottler
Within this lab I explored threads and how they're useful in data and task parallelization. There are two plants and within each plant there are 4 workers, each responsible for a different part of the orange juice making process. When one thread finishes a task it passes the orange onto the next thread to begin their work. This is done by utilizing the BlockingMailbox data structure which ensures that each worker is doing their given task.
