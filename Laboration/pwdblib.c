/*
 * Program: pwdblib.c
 *
 * Functions to access a local passwd file in the
 * current directory. See the #define of PWFILENAME in pwdblib.h for the
 * exact name of the file. The functions does not implement
 * any file lock, and not all possible error checks are done.
 * 
 */

#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stddef.h>
#include <sys/stat.h>
#include <unistd.h>
#include "pwdblib.h"

/*  PWFILENAME is defined in pwdblib.h */

/* Some generic error descriptions */
const char strPWDB_UNKNOWNERR[] = "Unknown error";
const char strPWDB_NOUSER[] = "Could not find user";
const char strPWDB_FILEERR[] = "File error";
const char strPWDB_MEMERR[] = "Could not allocate memory";
const char strPWDB_ENTRERR[] = "Error in passwd file entry";
const char strPWDB_NULL[] = "Function called with NULL argument";

/* Exported variables */
int pwdb_errno;

/* 
 * FUNCTION:
 * struct pwdb_passwd *pwdb_getpwnam(char *name);
 *
 *  Search the file PWFILENAME for an username entry matching name.
 *  Similare to the library call getpwnam().
 *  On success it return a pointer to a struct pwdb_passwd defined 
 *  in pwdblib.h. The return structure is malloced inside the function.
 *  If it fails it returns NULL and pwdb_errno is set to
 *  PWDB_NOUSER if the user name could not be found,
 *  PWDB_FILEERR if it could not access or find the file,
 *  PWDB_MEMERR if it could not allocate memory for the struct pwdb_passwd,
 *  and PWDB_ENTRERR if the line in PWFILENAME somehow has invalide format. 
 *
 */
struct pwdb_passwd *pwdb_getpwnam(const char *name)
{
  FILE *fd;
  char *entry, *item, *running;
  const char delimiters[] = ":\n";
  size_t entrysize;
  struct pwdb_passwd *p;
  int cleanup;
  int chread;

  if (name == NULL) {
    pwdb_errno=PWDB_NULL;
	return NULL;
  }

  fd=fopen(PWFILENAME,"r");
  if (!fd) { /* hmmm...try to create it instead */
    fd=fopen(PWFILENAME,"w+");
    if (!fd) { /* what the duck is going on?  */
       pwdb_errno=PWDB_FILEERR;
       return NULL;
    } else { /* if we just created the file, there aren't many users in it..*/
      fclose(fd);
      pwdb_errno=PWDB_NOUSER;
      return NULL;
    }
  }

  entrysize=0;
  entry=NULL;

  while (1) {
    if (feof(fd) || ((chread=getline(&entry,&entrysize,fd))==-1))
        goto _exit_nouser; /* no more users to read */

    /* This should probably not happend...but you'll never know*/    
    if (entry==NULL) goto _exit_nouser;
    
    if (*entry!='\n') { 
      running=entry;
      item=strsep(&running,delimiters);

      if (item==NULL) goto _exit_nouser;

      if (strcmp(item,name)==0) { /* found match */
	 p=(struct pwdb_passwd *)malloc(sizeof(struct pwdb_passwd));
         if (p==NULL) goto _exit_memerr;
         
	 p->pw_name=(char *)malloc(sizeof(char)*(strlen(item)+1));
         if (p->pw_name==NULL) goto _exit_memerr;
         strcpy(p->pw_name,item);

	 /* Extract password */
         item=strsep(&running,delimiters);
	 if (item==NULL) { cleanup=2; goto _entry_error; }
	 /* There is a bug in strsep so we must check for end of string also */
	 if (*item=='\0') { cleanup=2; goto _entry_error; }
	 p->pw_passwd=(char *)malloc(sizeof(char)*(strlen(item)+1));
         if (p->pw_passwd==NULL) goto _exit_memerr;
         strcpy(p->pw_passwd,item);

	 /* Extract uid */
         item=strsep(&running,delimiters);
	 if (item==NULL) { cleanup=3; goto _entry_error; }
	 if (*item=='\0') { cleanup=3; goto _entry_error; }
	 p->pw_uid=atoi(item);

	 /* Extract gid */
         item=strsep(&running,delimiters);
	 if (item==NULL) { cleanup=4; goto _entry_error; }
	 if (*item=='\0') { cleanup=4; goto _entry_error; }
	 p->pw_gid=atoi(item);

	 /* Extract real name */
         item=strsep(&running,delimiters);
	 if (item==NULL) { cleanup=5; goto _entry_error; }
	 if (*item=='\0') { cleanup=5; goto _entry_error; }
         p->pw_gecos=(char *)malloc(sizeof(char)*(strlen(item)+1));
         if (p->pw_gecos==NULL) goto _exit_memerr;
         strcpy(p->pw_gecos,item);

     /* Extract home directory */
	 item=strsep(&running, delimiters);
	 if (item == NULL) { cleanup=6; goto _entry_error; }
	 if (*item=='\0') { cleanup=6; goto _entry_error; }
	 p->pw_dir=(char *)malloc(strlen(item) + 1);
	 if (p->pw_dir == NULL) goto _exit_memerr;
	 strcpy(p->pw_dir, item);

	 /* Extract shell */
         item=strsep(&running,delimiters);
	 if (item==NULL) { cleanup=7; goto _entry_error; }
	 if (*item=='\0') { cleanup=7; goto _entry_error; }
         p->pw_shell=(char *)malloc(sizeof(char)*(strlen(item)+1));
         if (p->pw_shell==NULL) goto _exit_memerr;
         strcpy(p->pw_shell,item);

	 /* Extract failed */
         item=strsep(&running,delimiters);
	 if (item==NULL) { cleanup=8; goto _entry_error; }
	 if (*item=='\0') { cleanup=8; goto _entry_error; }
	 p->pw_failed=atoi(item);

	 /* Extract age */
         item=strsep(&running,delimiters);
	 if (item==NULL) { cleanup=9; goto _entry_error; }
	 if (*item=='\0') { cleanup=9; goto _entry_error; }
	 p->pw_age=atoi(item);

     goto _clean_exit;

     _entry_error:
       switch (cleanup) {
         case 9: 
         case 8: free(p->pw_shell);
         case 7: free(p->pw_dir);
         case 6 : free(p->pw_gecos);
         case 5 : 
         case 4 : 
         case 3 : free(p->pw_passwd);
         case 2 : free(p->pw_name);
         default : free(p); break;
       } /* end of switch */
	   free(entry);
         fclose(fd);
         pwdb_errno=PWDB_ENTRERR;
         return(NULL);
       } /* end of    if (strcmp(item,name) */
    } /* end of     if (*entry!='\n')  */
  }  /* end of while(1)  */


_exit_nouser:
  /* If we end up here, there is no match for the username */
  if ((entry!=NULL) && (entrysize>0)) free(entry);
  fclose(fd);
  pwdb_errno=PWDB_NOUSER;
  return(NULL);

_exit_memerr:
  if ((entry!=NULL) && (entrysize>0)) free(entry);
  fclose(fd);
  pwdb_errno=PWDB_MEMERR;
  return(NULL);

_clean_exit:
  free(entry);
  fclose(fd);
  pwdb_errno=PWDB_OK;  /* No error.. */
  return(p);
}



/*
 * FUNCTION:
 * int pwd_update_user(struct pwdb_passwd *p);
 *
 * Synopsis:
 *  #include "pwdblib.h"
 *
 * Search the file PWFILENAME for an entry matching the data in *p
 * If one is found, the entry is updated with the data in *p, otherwise
 * a new entry is created at the end of the file.
 * On success it returns 0.
 * If the file could not be accessed it returns PWDB_FILEERR
 * If memory could not be allocated it return PWDB_MEMERR
 * If p is null it returns PWDB_NULL
 */

int pwdb_update_user(struct pwdb_passwd *p){
  FILE *fd;
  char *entry,*buf,*tmp;
  char *uname;
  int unamesize,curr_unamesize,chrcpy;
  size_t filesize;
  int fdno;
  struct stat *filestat;

  if (p==NULL) return(PWDB_NULL);
  fd=fopen(PWFILENAME,"r+");
  if (!fd) { /* hmmm...try to create it instead */
    fd=fopen(PWFILENAME,"w+");
    if (!fd) { /* what is going on here?..Bail out  */
       return(PWDB_FILEERR);
    }
  }
  /* get file size in bytes */
  if ((fdno=fileno(fd))==-1) return(PWDB_FILEERR);
  filestat=(struct stat *)malloc(sizeof(struct stat));
  fstat(fdno,filestat);
  filesize=filestat->st_size;

  buf=(char *)malloc(sizeof(char)*(filesize+1));
  if (buf==NULL) { fclose(fd); return(PWDB_MEMERR); }
  fread(buf,sizeof(char),filesize,fd);
  buf[filesize]='\0';
  
  entry=buf;
  curr_unamesize=0;
  uname=NULL;
  while (1) {
    unamesize=strchr(entry,':')-entry+1;  /* size to include '\0' */
    if (unamesize<0) { /* no match */
        entry=NULL; break;  
    }
    if (unamesize>curr_unamesize) { /* resize uname */
        uname=(char *)realloc(uname,sizeof(char)*unamesize);
        if (uname==NULL) { 
	  free(buf);
          fclose(fd);
          return(PWDB_MEMERR);
	}
        curr_unamesize=unamesize;
    }
    /* here we should have a uname which is big enough for the user name */
    strncpy(uname,entry,unamesize-1);
    uname[unamesize-1]='\0';
     
    if (strcmp(p->pw_name,uname)==0) break;

    entry=strchr(entry,'\n');
    if (entry==NULL) break;
    while (*entry=='\n') entry++;
  }

  /* Now we have two cases; either entry==NULL and we should append
     the record, or entry point to a matched line in buf.
     In the second case we delete the entry from buf by moving 
     all entries below "up one line" and then append the changed 
     record to the end of the file. Quick and dirty? yea I know....*/
  if (entry!=NULL) {
    tmp=strchr(entry,'\n'); /* find next entry */
    if (tmp!=NULL) while (*tmp=='\n') tmp++;
    if (tmp==NULL)  /* already at end of file */
        chrcpy=0;
    else chrcpy=buf+filesize-tmp; 
    /* move all other entries */
    memmove(entry,tmp,chrcpy);   
    entry[chrcpy]='\0';
    /* rewind(fd); */
    fseek(fd,0L,SEEK_SET); 
    fwrite(buf,sizeof(char),(entry-buf)+chrcpy,fd);
  }
  /* append record */
  /* first flush so that stupid Solaris starts writing at the right position */
  fflush(fd);
  fprintf(fd,"%s:%s:%d:%d:%s:%s:%s:%d:%d\n",
	  p->pw_name,
	  p->pw_passwd,
	  p->pw_uid,
	  p->pw_gid,
	  p->pw_gecos,
	  p->pw_dir,
	  p->pw_shell,
	  p->pw_failed,
	  p->pw_age);

  fclose(fd);	  
  free(uname);
  free(buf);
  return(PWDB_OK);
}     



/* 
 * FUNCTION:
 * const char *pwdb_err2str(int errno);
 *
 * Synopsis:
 * #include "pwdblib.h"
 *
 * Return a pointer to a const char string with a short error description
 *
 */
const char *pwdb_err2str(int errno) {

  switch(errno) {
  case PWDB_NOUSER  : return(strPWDB_NOUSER);
  case PWDB_FILEERR : return(strPWDB_FILEERR);
  case PWDB_MEMERR  : return(strPWDB_MEMERR);
  case PWDB_ENTRERR : return(strPWDB_ENTRERR);
  case PWDB_NULL    : return(strPWDB_NULL);
  default :  return(strPWDB_UNKNOWNERR);
  }
}



#ifdef SUN
/* 
 * Implementation of functions that does not exist in SOLARIS libc. 
 * Be ware, not much error checking is done. Only the basic functionality
 * are implemented, and sometimes not even the basics...
 * Only included because students might need to test
 * there programs on SUN computers without glibc2
 *
 * Issues: 
 * 
 *   getline() cant be used to read lines containing '\0'
 * 
 */
ssize_t getline(char **lineptr,size_t *n,FILE *stream)
{
  ssize_t chread;

  /* Assume that a string of size 256 is enough....This is ugly! */
  if ((*n<256) || (*lineptr==NULL)) {
    *lineptr=(char *)realloc(*lineptr,sizeof(char)*256);
      if (*lineptr==NULL) return(-1);
      *n=256;
  }

  if (!fgets(*lineptr,*n,stream)) return(-1);
  /* Ok, scan **lineptr to see how many chars we have read */
  chread=0;
  while(*(*lineptr+chread)!='\0') chread++; 

  return(chread);
}


/* 
 * strsep is ripped from GNU libc2 so that should be OK ...
 */
char *strsep(char **stringp,const char *delim)
{
  char *begin, *end;

  begin = *stringp;
  if (begin == NULL)
    return NULL;

  /* A frequent case is when the delimiter string contains only one
     character.  Here we don't need to call the expensive `strpbrk'
     function and instead work using `strchr'.  */
  if (delim[0] == '\0' || delim[1] == '\0')
    {
      char ch = delim[0];

	if (ch == '\0')
	  end = NULL;  
	else
	  { 
	    if (*begin == ch)
	      end = begin;   
	    else if (*begin == '\0')
	      end = NULL;
	    else
	      end = strchr (begin + 1, ch);
	  }
	}
	else
    /* Find the end of the token.  */
    end = strpbrk (begin, delim);

  if (end)
    {
      /* Terminate the token and set *STRINGP past NUL character.  */
      *end++ = '\0';
      *stringp = end;
    }
  else
    /* No more delimiters; this is the last token.  */
    *stringp = NULL;

  return begin;
}



#endif
