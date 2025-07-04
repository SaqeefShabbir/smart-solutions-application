# Backup the database (run from command line)
pg_dump -U postgres -d smart_solutions -F c -b -v -f smart_solutions_backup.dump

# Restore the database (run from command line)
pg_restore -U postgres -d smart_solutions -v smart_solutions_backup.dump