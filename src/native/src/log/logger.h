#include <stdbool.h>

#ifdef __cplusplus
extern "C"
{
#endif

bool log_initialiseLogFile(char* name);
bool log_asInfo(char* message, ...);
bool log_asError(char* message, ...);

#ifdef __cplusplus
}
#endif
