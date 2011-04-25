#include "mutex.h"

void foo()
{
    mutex m1, m2;

/*    acquire(m1);
    acquire(m2);
    release(m1);
    release(m2);*/

    m1.acquire();
    m2.acquire();
    m1.release();
    m2.release();
}
