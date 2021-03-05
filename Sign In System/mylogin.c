/*
 * Shows user info from local pwfile.
 *  
 * Usage: userinfo username
 */

#define _XOPEN_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "pwdblib.h"   /* include header declarations for pwdblib.c */
#include <signal.h>
#include <unistd.h>

/* Define some constants. */
#define USERNAME_SIZE (32)
#define PASSWORD_SIZE (32)
#define NOUSER (-1)


int print_info(const char *username)
{
  struct pwdb_passwd *p = pwdb_getpwnam(username);
  if (p != NULL) {
    printf("Name: %s\n", p->pw_name);
    printf("Passwd: %s\n", p->pw_passwd);
    printf("Uid: %u\n", p->pw_uid);
    printf("Gid: %u\n", p->pw_gid);
    printf("Real name: %s\n", p->pw_gecos);
    printf("Home dir: %s\n",p->pw_dir);
    printf("Shell: %s\n", p->pw_shell);
	return 0;
  } else {
    return NOUSER;
  }
}

void read_username(char *username)
{
  printf("login: ");
  fgets(username, USERNAME_SIZE, stdin);

  /* remove the newline included by getline() */
  username[strlen(username) - 1] = '\0';
}

int read_password(const char *username)
{
  char *p = getpass("Password: "); //reads a string in from the terminal without echoing it
  struct pwdb_passwd *pw_struct = pwdb_getpwnam(username); // gets the password from the pwfile
  
  if(pw_struct == NULL) //if there is no password in the file for the username
  { 
  printf("Username or password is incorrect\n");
  return 0;
  }
  
  if(pw_struct->pw_failed > 5) //checks if user is locked out
  {
  printf("The user is locked out\n");
  return 0;
  }
  
  char *crypted_pass = crypt(p, pw_struct->pw_passwd); //compute the hash of the passwords

  if(strcmp(crypted_pass, pw_struct->pw_passwd) == 0) //checks if matching password
  {
  pw_struct->pw_age++; //increase age
  pw_struct->pw_failed = 0; //remove earlier failed attempts
  pwdb_update_user(pw_struct); //update file
  
  if(pw_struct->pw_age > 4) //if password is getting "old"
  {
  printf("This password is getting old, please change it\n");
  }
  
  printf("User authenticated successfully\n");
  shell(pw_struct);
  return 1;
  }

//if the password was incorrect the following is executed
  pw_struct->pw_failed++;
  pwdb_update_user(pw_struct);
  
  printf("Username or password is incorrect\n");
  printf("Number of failed attempts: %i\n",pw_struct->pw_failed);
  return 0;
}

#define PROGRAM "/usr/bin/xterm"

int shell(struct pwdb_passwd *pw_struct)
{
  pid_t pid; 
  int status;

  pid = fork();

  if (pid==0) {
     setuid(pw_struct->pw_uid);
      setgid(pw_struct->pw_gid);
      printf("%d", getuid());
	  printf("%d", getgid());
    /* This is the child process. Run an xterm window */
    execl(PROGRAM,PROGRAM,"-e","user shell","-l",NULL);

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

int main(int argc, char **argv)
{
	 signal(SIGINT, catch_int);
  char username[USERNAME_SIZE];
  char i = 0;
  
  while(i == 0)
  {
  /* 
   * Write "login: " and read user input. Copies the username to the
   * username variable.
   */
  read_username(username);
  //read_password(username);
  i = read_password(username);
  }
  
}