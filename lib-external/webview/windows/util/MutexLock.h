/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

#include "stdafx.h"

#ifndef MUTEX_LOCK_H
#define MUTEX_LOCK_H

/**
 * Lock on a Windows mutex. The lock is acquired when the MutexLock object is created,
 * and is released when the object is destroyed.
 *
 * Usage:
 * .... non-critical code ...
 * {
 *     MutexLock lock(mutex); // Mutex is acquired
 *     ... critical code ...
 * } // lock object is destroyed, releasing mutex
 *
 * @author pabercrombie
 * @version $Id: MutexLock.h 1171 2013-02-11 21:45:02Z dcollins $
 */
class MutexLock {
public:
    /** Create a mutex lock. */
    MutexLock(HANDLE mutex)
        : mutex(mutex)
    {
        DWORD dwWaitResult = WaitForSingleObject(mutex, INFINITE);
        if (dwWaitResult != WAIT_OBJECT_0)
        {
            Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", GetLastError());
        }
    }

    /** Destroy the mutex lock. */
    ~MutexLock() {
        ReleaseMutex(mutex);
    }

protected:
    HANDLE mutex;
};

#endif
