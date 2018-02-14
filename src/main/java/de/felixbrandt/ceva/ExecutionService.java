package de.felixbrandt.ceva;

import de.felixbrandt.ceva.controller.CommandFactory;
import de.felixbrandt.ceva.controller.DataSourceFilter;
import de.felixbrandt.ceva.controller.RunVersionProvider;
import de.felixbrandt.ceva.controller.ShellCommandFactory;
import de.felixbrandt.ceva.controller.base.VersionProvider;
import de.felixbrandt.ceva.database.SessionHandler;
import de.felixbrandt.ceva.provider.AlgorithmDBProvider;
import de.felixbrandt.ceva.provider.DataSourceProvider;
import de.felixbrandt.ceva.provider.ExecutableProvider;
import de.felixbrandt.ceva.provider.InstanceDBProvider;
import de.felixbrandt.ceva.provider.InstanceMetricDBProvider;
import de.felixbrandt.ceva.provider.SolutionDBProvider;
import de.felixbrandt.ceva.provider.SolutionMetricDBProvider;
import de.felixbrandt.ceva.provider.UnsolvedProvider;
import de.felixbrandt.ceva.storage.base.Storage;

/**
 * Strategy to execute all missing calculations (metrics and experiments).
 */
public class ExecutionService
{
  private final SessionHandler session_handler;
  private final ExecutionStrategy execution_strategy;

  protected ExecutionService(final SessionHandler handler, final ExecutionStrategy strategy)
  {
    session_handler = handler;
    execution_strategy = strategy;
  }

  public final void run ()
  {
    runInstanceData();
    runAlgorithms();
    runSolutionData();
  }

  public final VersionProvider setupVersionProvider ()
  {
    final CommandFactory command_factory = new ShellCommandFactory();
    return new RunVersionProvider(command_factory);
  }

  private final InstanceDBProvider setupInstanceProvider ()
  {
    return new InstanceDBProvider(session_handler);
  }

  private final DataSourceFilter setupUnsolvedFilter (UnsolvedProvider provider)
  {
    return new UnsolvedSourcesFilter(provider, setupVersionProvider());
  }

  public final void runAlgorithms ()
  {
    final AlgorithmDBProvider executables = new AlgorithmDBProvider(session_handler);
    final SolutionDBStorage solution_storage = new SolutionDBStorage(session_handler);

    doRun(executables, setupInstanceProvider(), solution_storage,
            setupUnsolvedFilter(solution_storage));
  }

  public final void runInstanceData ()
  {
    final InstanceMetricDBProvider executables = new InstanceMetricDBProvider(session_handler);
    final DataDBStorage data_storage = new InstanceDataDBStorage(session_handler);

    doRun(executables, setupInstanceProvider(), data_storage,
            setupUnsolvedFilter(data_storage));
  }

  public final void runSolutionData ()
  {
    final SolutionMetricDBProvider executables = new SolutionMetricDBProvider(session_handler);
    final SolutionDBProvider sources = new SolutionDBProvider(session_handler);
    final DataDBStorage data_storage = new SolutionDataDBStorage(session_handler);

    doRun(executables, sources, data_storage, setupUnsolvedFilter(data_storage));
  }

  public final void doRun (final ExecutableProvider executable_provider,
          final DataSourceProvider source_provider, final Storage storage,
          final DataSourceFilter filter)
  {
    execution_strategy.run(executable_provider, source_provider, storage, filter);
  }

}
