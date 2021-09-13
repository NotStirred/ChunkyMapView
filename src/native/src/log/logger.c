#include "logger.h"
#include <time.h>
#include <stdarg.h>
#include <stdio.h>

char* logFile;

bool log_initialiseLogFile(char* name)
{
	logFile = name;
	FILE* file = fopen(logFile, "w");
	if(!file)
	{
		fprintf(stderr, "ERROR: could not open LOG_FILE %s for writing\n", logFile);
		return false;
	}
	time_t now = time(NULL);
	char* date = ctime(&now);
	fprintf(file, "[%.24s] LOG_FILE Initialise\n", date);
	fclose(file);
	return true;
}

bool log_asInfo(char* message, ...) {
	va_list argptr;

	FILE* file = fopen(logFile, "a");
	if(!file) {
		fprintf(stderr, "ERROR: could not open LOG_FILE %s for appending\n", logFile);
		return false;
	}
	time_t now = time(NULL);
	char* date = ctime(&now);
	fprintf(file, "[%.24s] INFO | ", date);
    printf("[%.24s] INFO | ", date);

	va_start(argptr, message);
	vfprintf(file, message, argptr);
	va_end(argptr);

	//fprintf(file, "[%.24s] %s\n", date, message);
	fclose(file);
	
	return true;
}

bool log_asError(char* message, ...)
{
	va_list argptr;

	FILE* file = fopen(logFile, "a");
	if(!file) {
		fprintf(stderr, "ERROR: could not open LOG_FILE %s for appending\n", logFile);
		return false;
	}
	time_t now = time(NULL);
	char* date = ctime(&now);
	fprintf(file, "[%.24s] ERROR | ", date);
	printf("[%.24s] ERROR | ", date);

	va_start(argptr, message);
	vfprintf(file, message, argptr);
	va_end(argptr);

	va_start(argptr, message);
	vprintf(message, argptr);
	va_end(argptr);

	//fprintf(file, "[%.24s] %s\n", date, message);
	fclose(file);
	
	return true;
}

