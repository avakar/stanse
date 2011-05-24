/*
 * You should check this file with alloc.xml automaton,
 * which matches against "assign &%1, (call operator new, *)"
 * to detect allocations and "call __sir_cpp_delete, %1, *"
 * to detect releases.
 *
 * You can also merge translated version of this file,
 * which I saved for your convenience in this directory under
 * the name alloc.sir, with the translated version of alloc_deleter.cpp
 * (again, stored under alloc_deleter.sir) to get an additional error trace
 * through late binding. Once again, the merged file is available
 * for your convenience under the name alloc_merged.sir.
 */

/*
 * This is a straight-forward bug; we allocate but fail to release.
 * The bug will disappear if you uncomment the second line.
 */
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

/*
 * This function checks whether processing succeeded and fails early if not,
 * failing to release data in such a case.
 */
bool f2()
{
    int * data = new int(42);
    if (!process_data(*data))
        return false;
    return discard_data(data);
}

/*
 * This function has exactly the same control flow as f2().
 * The example demonstrates that intra-statement control-flow
 * is correctly captured and multiple calls per statement correctly
 * handled.
 */
bool f3()
{
    int * data = new int(42);
    return process_data(*data) && discard_data(data);
}

/*
 * There is no bug in this function. You can create one
 * by swapping the operands of the && operator.
 */
bool f4()
{
    int * data = new int(42);
    bool success = process_data(*data);
    return discard_data(data) && success;
}

struct discarder_base
{
    virtual void discard(int * data) = 0;
};

struct discarder_correct
    : discarder_base
{
    virtual void discard(int * data)
    {
        delete data;
    }
};

/*
 * This function releases data through a virtual call.
 * The only possible callee in this translation unit is
 * discarder_correct::discard, which will delete the data correctly.
 *
 * If you merge this unit with alloc_deleter, you'll get an additional
 * error trace here (through discarder_faulty::discard).
 */
bool f5(discarder_base & d)
{
    int * data = new int(42);
    bool success = process_data(*data);
    d.discard(data);
    return success;
}
