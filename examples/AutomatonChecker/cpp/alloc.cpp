/* Here we allocate, but fail to release. */
void f1()
{
    int * data = new int(42);
    //delete data;
}

bool process_data(int data);

bool discard_data(int * data)
{
    delete data;
    return true;
}

bool f2()
{
    int * data = new int(42);
    if (!process_data(*data))
        return false;
    return discard_data(data);
}

bool f3()
{
    int * data = new int(42);
    return process_data(*data) && discard_data(data);
}

bool f4()
{
    int * data = new int(42);
    bool success = process_data(*data);
    return discard_data(data) && success;
}

struct discarder_base
{
    virtual void discard(int * data)
    {
        discard_data(data);
    }
};

struct discarder_advanced
    : discarder_base
{
    virtual void discard(int * data)
    {
        //discard_data(data);
    }
};

bool f5(discarder_base & d)
{
    int * data = new int(42);
    bool success = process_data(*data);
    d.discard(data);
    return success;
}
