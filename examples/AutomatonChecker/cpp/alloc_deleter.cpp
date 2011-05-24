/*
 * This file does not produce any error traces on its own.
 * It will, however, create an additional error trace when
 * merged with alloc.cpp.
 */

struct discarder_base
{
    virtual void discard(int * data) = 0;
};

struct discarder_faulty
    : discarder_base
{
    virtual void discard(int * data)
    {
        delete[] data;
    }
};
