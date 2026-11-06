package com.example.moneymate;

import androidx.annotation.NonNull;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenDelegate;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.SQLite;
import androidx.sqlite.SQLiteConnection;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class MmDatabase_Impl extends MmDatabase {
  private volatile MmDao _mmDao;

  @Override
  @NonNull
  protected RoomOpenDelegate createOpenDelegate() {
    final RoomOpenDelegate _openDelegate = new RoomOpenDelegate(2, "56305de2d2fe2d0067a240e310d848ab", "4510f4d3d779b9f1e804e7cd7e6d51ef") {
      @Override
      public void createAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `accounts` (`id` TEXT NOT NULL, `uid` TEXT NOT NULL, `name` TEXT NOT NULL, `lastFour` TEXT NOT NULL, `balanceCents` INTEGER NOT NULL, `dirty` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `transactions` (`id` TEXT NOT NULL, `uid` TEXT NOT NULL, `merchant` TEXT NOT NULL, `category` TEXT NOT NULL, `amountCents` INTEGER NOT NULL, `date` TEXT NOT NULL, `accountId` TEXT NOT NULL, `dirty` INTEGER NOT NULL, `receiptUrl` TEXT NOT NULL, PRIMARY KEY(`id`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `budgets` (`id` TEXT NOT NULL, `uid` TEXT NOT NULL, `category` TEXT NOT NULL, `limitCents` INTEGER NOT NULL, `dirty` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `goals` (`id` TEXT NOT NULL, `uid` TEXT NOT NULL, `name` TEXT NOT NULL, `targetCents` INTEGER NOT NULL, `savedCents` INTEGER NOT NULL, `dirty` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `profile` (`uid` TEXT NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, PRIMARY KEY(`uid`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `settings` (`uid` TEXT NOT NULL, `currency` TEXT NOT NULL, `theme` TEXT NOT NULL, `hideBalances` INTEGER NOT NULL, `budgetAlerts` INTEGER NOT NULL, `dirty` INTEGER NOT NULL, PRIMARY KEY(`uid`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `deleted` (`key` TEXT NOT NULL, `uid` TEXT NOT NULL, `collection` TEXT NOT NULL, `docId` TEXT NOT NULL, PRIMARY KEY(`key`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        SQLite.execSQL(connection, "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '56305de2d2fe2d0067a240e310d848ab')");
      }

      @Override
      public void dropAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `accounts`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `transactions`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `budgets`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `goals`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `profile`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `settings`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `deleted`");
      }

      @Override
      public void onCreate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      public void onOpen(@NonNull final SQLiteConnection connection) {
        internalInitInvalidationTracker(connection);
      }

      @Override
      public void onPreMigrate(@NonNull final SQLiteConnection connection) {
        DBUtil.dropFtsSyncTriggers(connection);
      }

      @Override
      public void onPostMigrate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      @NonNull
      public RoomOpenDelegate.ValidationResult onValidateSchema(
          @NonNull final SQLiteConnection connection) {
        final Map<String, TableInfo.Column> _columnsAccounts = new HashMap<String, TableInfo.Column>(6);
        _columnsAccounts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("uid", new TableInfo.Column("uid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("lastFour", new TableInfo.Column("lastFour", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("balanceCents", new TableInfo.Column("balanceCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("dirty", new TableInfo.Column("dirty", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysAccounts = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesAccounts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAccounts = new TableInfo("accounts", _columnsAccounts, _foreignKeysAccounts, _indicesAccounts);
        final TableInfo _existingAccounts = TableInfo.read(connection, "accounts");
        if (!_infoAccounts.equals(_existingAccounts)) {
          return new RoomOpenDelegate.ValidationResult(false, "accounts(com.example.moneymate.AccountEntity).\n"
                  + " Expected:\n" + _infoAccounts + "\n"
                  + " Found:\n" + _existingAccounts);
        }
        final Map<String, TableInfo.Column> _columnsTransactions = new HashMap<String, TableInfo.Column>(9);
        _columnsTransactions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("uid", new TableInfo.Column("uid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("merchant", new TableInfo.Column("merchant", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("amountCents", new TableInfo.Column("amountCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("accountId", new TableInfo.Column("accountId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("dirty", new TableInfo.Column("dirty", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("receiptUrl", new TableInfo.Column("receiptUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysTransactions = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesTransactions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTransactions = new TableInfo("transactions", _columnsTransactions, _foreignKeysTransactions, _indicesTransactions);
        final TableInfo _existingTransactions = TableInfo.read(connection, "transactions");
        if (!_infoTransactions.equals(_existingTransactions)) {
          return new RoomOpenDelegate.ValidationResult(false, "transactions(com.example.moneymate.TxEntity).\n"
                  + " Expected:\n" + _infoTransactions + "\n"
                  + " Found:\n" + _existingTransactions);
        }
        final Map<String, TableInfo.Column> _columnsBudgets = new HashMap<String, TableInfo.Column>(5);
        _columnsBudgets.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgets.put("uid", new TableInfo.Column("uid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgets.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgets.put("limitCents", new TableInfo.Column("limitCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgets.put("dirty", new TableInfo.Column("dirty", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysBudgets = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesBudgets = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBudgets = new TableInfo("budgets", _columnsBudgets, _foreignKeysBudgets, _indicesBudgets);
        final TableInfo _existingBudgets = TableInfo.read(connection, "budgets");
        if (!_infoBudgets.equals(_existingBudgets)) {
          return new RoomOpenDelegate.ValidationResult(false, "budgets(com.example.moneymate.BudgetEntity).\n"
                  + " Expected:\n" + _infoBudgets + "\n"
                  + " Found:\n" + _existingBudgets);
        }
        final Map<String, TableInfo.Column> _columnsGoals = new HashMap<String, TableInfo.Column>(6);
        _columnsGoals.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoals.put("uid", new TableInfo.Column("uid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoals.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoals.put("targetCents", new TableInfo.Column("targetCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoals.put("savedCents", new TableInfo.Column("savedCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGoals.put("dirty", new TableInfo.Column("dirty", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysGoals = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesGoals = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGoals = new TableInfo("goals", _columnsGoals, _foreignKeysGoals, _indicesGoals);
        final TableInfo _existingGoals = TableInfo.read(connection, "goals");
        if (!_infoGoals.equals(_existingGoals)) {
          return new RoomOpenDelegate.ValidationResult(false, "goals(com.example.moneymate.GoalEntity).\n"
                  + " Expected:\n" + _infoGoals + "\n"
                  + " Found:\n" + _existingGoals);
        }
        final Map<String, TableInfo.Column> _columnsProfile = new HashMap<String, TableInfo.Column>(3);
        _columnsProfile.put("uid", new TableInfo.Column("uid", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfile.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfile.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysProfile = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesProfile = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProfile = new TableInfo("profile", _columnsProfile, _foreignKeysProfile, _indicesProfile);
        final TableInfo _existingProfile = TableInfo.read(connection, "profile");
        if (!_infoProfile.equals(_existingProfile)) {
          return new RoomOpenDelegate.ValidationResult(false, "profile(com.example.moneymate.ProfileEntity).\n"
                  + " Expected:\n" + _infoProfile + "\n"
                  + " Found:\n" + _existingProfile);
        }
        final Map<String, TableInfo.Column> _columnsSettings = new HashMap<String, TableInfo.Column>(6);
        _columnsSettings.put("uid", new TableInfo.Column("uid", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSettings.put("currency", new TableInfo.Column("currency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSettings.put("theme", new TableInfo.Column("theme", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSettings.put("hideBalances", new TableInfo.Column("hideBalances", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSettings.put("budgetAlerts", new TableInfo.Column("budgetAlerts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSettings.put("dirty", new TableInfo.Column("dirty", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysSettings = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesSettings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSettings = new TableInfo("settings", _columnsSettings, _foreignKeysSettings, _indicesSettings);
        final TableInfo _existingSettings = TableInfo.read(connection, "settings");
        if (!_infoSettings.equals(_existingSettings)) {
          return new RoomOpenDelegate.ValidationResult(false, "settings(com.example.moneymate.SettingsEntity).\n"
                  + " Expected:\n" + _infoSettings + "\n"
                  + " Found:\n" + _existingSettings);
        }
        final Map<String, TableInfo.Column> _columnsDeleted = new HashMap<String, TableInfo.Column>(4);
        _columnsDeleted.put("key", new TableInfo.Column("key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeleted.put("uid", new TableInfo.Column("uid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeleted.put("collection", new TableInfo.Column("collection", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeleted.put("docId", new TableInfo.Column("docId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysDeleted = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesDeleted = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDeleted = new TableInfo("deleted", _columnsDeleted, _foreignKeysDeleted, _indicesDeleted);
        final TableInfo _existingDeleted = TableInfo.read(connection, "deleted");
        if (!_infoDeleted.equals(_existingDeleted)) {
          return new RoomOpenDelegate.ValidationResult(false, "deleted(com.example.moneymate.DeletedEntity).\n"
                  + " Expected:\n" + _infoDeleted + "\n"
                  + " Found:\n" + _existingDeleted);
        }
        return new RoomOpenDelegate.ValidationResult(true, null);
      }
    };
    return _openDelegate;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final Map<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final Map<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "accounts", "transactions", "budgets", "goals", "profile", "settings", "deleted");
  }

  @Override
  public void clearAllTables() {
    super.performClear(false, "accounts", "transactions", "budgets", "goals", "profile", "settings", "deleted");
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final Map<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MmDao.class, MmDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final Set<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MmDao mm() {
    if (_mmDao != null) {
      return _mmDao;
    } else {
      synchronized(this) {
        if(_mmDao == null) {
          _mmDao = new MmDao_Impl(this);
        }
        return _mmDao;
      }
    }
  }
}
