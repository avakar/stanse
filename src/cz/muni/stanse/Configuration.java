package cz.muni.stanse;

import cz.muni.stanse.checker.CheckerError;
import cz.muni.stanse.checker.CheckerException;
import cz.muni.stanse.checker.CheckerErrorReceiver;
import cz.muni.stanse.gui.AllOpenedFilesEnumerator;
import cz.muni.stanse.utils.Make;
import cz.muni.stanse.utils.ClassLogger;
import cz.muni.stanse.utils.ProgressMonitor;
import cz.muni.stanse.utils.ColumnMessageFormater;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.io.File;

public final class Configuration {

    // public section

    public Configuration() {
        sourceConfiguration = createDefaultSourceConfiguration();
        checkerConfigurations = createDefaultCheckerConfiguration();
    }

    public Configuration(final SourceConfiguration sourceConfiguration) {
        this.sourceConfiguration = sourceConfiguration;
        checkerConfigurations = createDefaultCheckerConfiguration();
    }

    public Configuration(final SourceConfiguration sourceConfiguration,
                  final List<CheckerConfiguration> checkerConfiguration) {
        this.sourceConfiguration = sourceConfiguration;
        this.checkerConfigurations = checkerConfiguration;
    }

    public void evaluate(final CheckerErrorReceiver receiver) {
        evaluate(receiver,
                 new ProgressMonitor() {
                    @Override
                    public void write(final String s) {
                    }
                 });
    }

    public void evaluate(final CheckerErrorReceiver receiver,
                         final ProgressMonitor monitor) {
        final java.util.Vector<Integer> numCheckerCongfigs =
                new java.util.Vector<Integer>();
        numCheckerCongfigs.add(getCheckerConfigurations().size());
        int threadID = 0;
        for (final CheckerConfiguration checkerCfg : getCheckerConfigurations())
            new MonitoredThread(new MonitorForThread(++threadID,monitor)) {
                @Override
                public void run() {
                    try {
                        checkerCfg.getChecker().check(
                            checkerCfg.isInterprocedural() ?
                                    getSourceConfiguration()
                                       .getLazySourceInternals() :
                                    getSourceConfiguration()
                                       .getLazySourceIntraproceduralInternals(),
                            receiver,getMonitor());
                    }
                    catch (final CheckerException e) {
                        ClassLogger.error(Configuration.class,
                            "evalueate() failed :: when running configuration "+
                            checkerCfg.getCheckerClassName() + "arguments " +
                            checkerCfg.getCheckerArgumentsList() + " this " +
                            "exception arose:\n" + e.getStackTrace());
                    }
                    catch (final Exception e) {
                        ClassLogger.error(Configuration.class,
                            "evalueate() failed :: unknown - configuration=["+
                            checkerCfg.getCheckerClassName() + ", " +
                            checkerCfg.getCheckerArgumentsList() +
                            "] exception:\n" + e.getStackTrace());
                    }
                    finally {
                        synchronized(this.getClass()) {
                            assert(numCheckerCongfigs.get(0) > 0);
                            numCheckerCongfigs.set(0,
                                         numCheckerCongfigs.firstElement() - 1);
                            if (numCheckerCongfigs.get(0) == 0)
                                receiver.onEnd();
                        }
                    }
                }
            }.start();
    }

    public void evaluateWait(final CheckerErrorReceiver receiver) {
        evaluateWait(receiver,
                     new ProgressMonitor() {
                        @Override
                        public void write(final String s) {
                        }
                     });
    }

    public void evaluateWait(final CheckerErrorReceiver receiver,
                             final ProgressMonitor monitor) {
        int threadID = 0;
        for (final CheckerConfiguration checkerCfg : getCheckerConfigurations())
            try {
                checkerCfg.getChecker().check(
                    checkerCfg.isInterprocedural() ?
                            getSourceConfiguration()
                               .getLazySourceInternals() :
                            getSourceConfiguration()
                               .getLazySourceIntraproceduralInternals(),
                    receiver,new MonitorForThread(++threadID,monitor));
            }
            catch (final CheckerException e) {
                ClassLogger.error(Configuration.class,
                    "evalueateWait() failed :: when running configuration "+
                    checkerCfg.getCheckerClassName() + "arguments " +
                    checkerCfg.getCheckerArgumentsList() + " this " +
                    "exception arose:\n" + e.getStackTrace());
            }
            catch (final Exception e) {
                ClassLogger.error(Configuration.class,
                    "evalueateWait() failed :: unknown - configuration=["+
                    checkerCfg.getCheckerClassName() + ", " +
                    checkerCfg.getCheckerArgumentsList() +
                    "] exception:\n" + e.getStackTrace());
            }
        receiver.onEnd();
    }

    @Deprecated
    public void
    evaluate_EachUnitSeparatelly(final CheckerErrorReceiver receiver) {
        evaluate_EachUnitSeparatelly(receiver,
                                     new ProgressMonitor() {
                                        @Override
                                        public void write(final String s) {
                                        }
                                     });
    }

    @Deprecated
    public void
    evaluate_EachUnitSeparatelly(final CheckerErrorReceiver receiver,
                                 final ProgressMonitor monitor) {
        new java.lang.Thread() {
            @Override
            public void run() {
                final CheckerErrorReceiver receiverWrapper =
                    new CheckerErrorReceiver() {
                        @Override
                        public void receive(final CheckerError error) {
                            receiver.receive(error);
                        }
                    };
                try {
                    for (final String fileName : getSourceConfiguration()
                                                        .getSourceEnumerator()
                                                        .getSourceCodeFiles()) {
                        monitor.write("<-> File: " + fileName + "\n");
                        new Configuration(
                            new SourceConfiguration(new FileListEnumerator(
                                                    Make.linkedList(fileName))),
                            getCheckerConfigurations())
                        .evaluateWait(receiverWrapper,monitor);
                        monitor.write("<-> --------------------------------\n");
                    }
                }
                catch (final SourceCodeFilesException e) {
                    ClassLogger.error(Configuration.class,
                        "evalueateWait_EachUnitSeparatelly() failed :: " +
                        "due to this exception :\n" +
                        e.getStackTrace());
                }
                receiver.onEnd();
            }
        }.start();
    }

    public SourceConfiguration getSourceConfiguration() {
        return sourceConfiguration;
    }

    public List<CheckerConfiguration> getCheckerConfigurations() {
        return checkerConfigurations;
    }

    // private section

    private final class MonitorForThread implements ProgressMonitor {
        MonitorForThread(int threadID, final ProgressMonitor monitor) {
            super();
            formater = new ColumnMessageFormater("<" + threadID + "> ",1);
            this.monitor = monitor;
        }

        @Override
        public void write(final String s) {
            monitor.write(formater.write(s + (s.endsWith("\n") ? "" : "\n")));
        }

        private final ColumnMessageFormater formater;
        private final ProgressMonitor monitor;
    }

    private class MonitoredThread extends java.lang.Thread {
        MonitoredThread(final MonitorForThread monitor) {
            super();
            this.monitor = monitor;
        }

        final MonitorForThread getMonitor() {
            return monitor;
        }

        private final MonitorForThread monitor;
    }

    private static SourceConfiguration createDefaultSourceConfiguration() {
        return new SourceConfiguration(new AllOpenedFilesEnumerator());
    }

    private static LinkedList<CheckerConfiguration>
	    createDefaultCheckerConfiguration() {
        return Make.<CheckerConfiguration>linkedList(
            new CheckerConfiguration("AutomatonChecker", Make.<File>linkedList(
                        new File(Stanse.getRootDirectory() +
                              "/data/checkers/AutomatonChecker/memory.xml"),
                        new File(Stanse.getRootDirectory() +
                              "/data/checkers/AutomatonChecker/interrupts.xml"),
                        new File(Stanse.getRootDirectory() +
                              "/data/checkers/AutomatonChecker/locking.xml")
                ),true));
    }

    private final SourceConfiguration sourceConfiguration;
    private final List<CheckerConfiguration> checkerConfigurations;
}
