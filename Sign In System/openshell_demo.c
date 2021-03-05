/*
 * Program: openshell_demo.c
 * 
 * Synopsis:
 *
 * Creates a new child process which starts a xterm window.
 * The parent just waits for the child to complete, and then exits.
 *
 */
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>

#define PROGRAM "/usr/bin/xterm"

int main() {
  pid_t pid; 
  int status;

  pid = fork();

  if (pid==0) {
    /* This is the child process. Run an xterm window */
    execl(PROGRAM,PROGRAM,"-e","/bin/sh","-l",NULL);

    /* if child returns we must inform parent.
     * Always exit a child process with _exit() and not return() or exit().
     */
    _exit(-1);
  } else if (pid < 0) { /* Fork failed */
    printf("Fork faild\n");
    status = -1;
  } else {
    /* This is parent process. Wait for child to complete */
	if (waitpid(pid, &status, 0) != pid) {
	  status = -1;
	}
  }

  return status;
}
