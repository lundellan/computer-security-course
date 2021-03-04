/*
 * Depends on: pwdblib.c
 *
 * Synopsis:   
 *
 * Updates a users entry in the passwd file.
 * It ask for new data for each item in struct pwdb_passwd
 * If you just press enter for any question, the old information 
 * will be unchanged.
 * If the user does not exist, it will add a new entry in the passwd file.
 * The entered password will be stored in plaintext.
 *
 *  Usage: update_user username
 *
 */
#define _GNU_SOURCE 
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "pwdblib.h"    /* include header declarations for pwdblib.c */

int main(int argc,char **argv)
{
  char *username;
  char *buf;
  size_t buflen, chread;
  struct pwdb_passwd *p, *oldp;
  int updt;

  if (argc<2) {
    printf("Usage: update_user username\n");
    return(0);
  }

  username=argv[1];
  oldp=pwdb_getpwnam(username);
  if (oldp!=NULL) 
     updt=1;
  else if ((oldp==NULL) && (pwdb_errno==PWDB_NOUSER)) 
          updt=0; 
  else {
    printf("pwdb_getpwnam return error: %s\n",pwdb_err2str(pwdb_errno));
    return(0);
  }

  p=(struct pwdb_passwd *)malloc(sizeof(struct pwdb_passwd));
  buf=NULL;
  buflen=0;


  p->pw_name=username;
 
  chread=0;
  while (1) {
    printf("Password:");
    chread=getline(&buf,&buflen,stdin); 
    buf[chread]='\0'; chread--; /* remove '\n' included by getline */
    if ((!updt) && (chread<1)) 
       printf("New user, so you must enter data\n");
    if ((updt) && (chread<1)) {
      p->pw_passwd=oldp->pw_passwd;
      break;
    }
    if (chread>0) {
       p->pw_passwd=(char *)malloc(sizeof(char)*(chread+1));
       strncpy(p->pw_passwd,buf,chread);
       p->pw_passwd[chread]='\0';
       break;
     }  
  }

  chread=0;
  while (1) {
    printf("Uid:");
    chread=getline(&buf,&buflen,stdin); 
    buf[chread]='\0'; chread--; /* remove '\n' included by getline */
    if ((!updt) && (chread<1)) 
       printf("New user, so you must enter data\n");
    if ((updt) && (chread<1)) {
      p->pw_uid=oldp->pw_uid;
      break;
    }
    if (chread>0) {
       p->pw_uid=atoi(buf);
       break;
     }  
  }

  chread=0;
  while (1) {
    printf("Gid:");
    chread=getline(&buf,&buflen,stdin); 
    buf[chread]='\0'; chread--; /* remove '\n' included by getline */
    if ((!updt) && (chread<1)) 
       printf("New user, so you must enter data\n");
    if ((updt) && (chread<1)) {
      p->pw_gid=oldp->pw_gid;
      break;
    }
    if (chread>0) {
       p->pw_gid=atoi(buf);
       break;
     }  
  }

 chread=0;
  while (1) {
    printf("Real name:");
    chread=getline(&buf,&buflen,stdin); 
    buf[chread]='\0'; chread--; /* remove '\n' included by getline */
    if ((!updt) && (chread<1)) 
       printf("New user, so you must enter data\n");
    if ((updt) && (chread<1)) {
      p->pw_gecos=oldp->pw_gecos;
      break;
    }
    if (chread>0) {
       p->pw_gecos=(char *)malloc(sizeof(char)*(chread+1));
       strncpy(p->pw_gecos,buf,chread);
       p->pw_gecos[chread]='\0';
       break;
     }  
  }

  chread=0;
  while (1) {
    printf("Home directory:");
    chread=getline(&buf,&buflen,stdin); 
    buf[chread]='\0'; chread--; /* remove '\n' included by getline */
    if ((!updt) && (chread<1)) 
       printf("New user, so you must enter data\n");
    if ((updt) && (chread<1)) {
      p->pw_dir=oldp->pw_dir;
      break;
    }
    if (chread>0) {
       p->pw_dir=(char *)malloc(sizeof(char)*(chread+1));
       strncpy(p->pw_dir,buf,chread);
       p->pw_dir[chread]='\0';
       break;
     }  
  }

  chread=0;
  while (1) {
    printf("Shell:");
    chread=getline(&buf,&buflen,stdin); 
    buf[chread]='\0'; chread--; /* remove '\n' included by getline */
    if ((!updt) && (chread<1)) 
       printf("New user, so you must enter data\n");
    if ((updt) && (chread<1)) {
      p->pw_shell=oldp->pw_shell;
      break;
    }
    if (chread>0) {
       p->pw_shell=(char *)malloc(sizeof(char)*(chread+1));
       strncpy(p->pw_shell,buf,chread);
       p->pw_shell[chread]='\0';
       break;
     }  
  }

  chread=0;
  while (1) {
    printf("Failed:");
    chread=getline(&buf,&buflen,stdin); 
    buf[chread]='\0'; chread--; /* remove '\n' included by getline */
    if ((!updt) && (chread<1)) 
       printf("New user, so you must enter data\n");
    if ((updt) && (chread<1)) {
      p->pw_failed=oldp->pw_failed;
      break;
    }
    if (chread>0) {
       p->pw_failed=atoi(buf);
       break;
     }  
  }


  chread=0;
  while (1) {
    printf("Age:");
    chread=getline(&buf,&buflen,stdin); 
    buf[chread]='\0'; chread--; /* remove '\n' included by getline */
    if ((!updt) && (chread<1)) 
       printf("New user, so you must enter data\n");
    if ((updt) && (chread<1)) {
      p->pw_age=oldp->pw_age;
      break;
    }
    if (chread>0) {
       p->pw_age=atoi(buf);
       break;
     }  
  }


  if (pwdb_update_user(p)!=0) {
      printf("pwdb_update_user returned error %s\n",pwdb_err2str(pwdb_errno));
  }
  

  return(0);
}
  

  
