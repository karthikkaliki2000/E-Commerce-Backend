Docker MySQL Backup & Restore Notes
🔒 Backup MySQL volume (snapshot method)
This creates a full snapshot of the MySQL data directory inside the Docker volume. Use this when you want a quick rollback or disaster recovery of your local environment.

powershell
# Backup entire MySQL volume (jwt_mysql_data)
docker run --rm -v jwt_mysql_data:/var/lib/mysql -v ${PWD}:/backup busybox tar cvf /backup/mysql_data_backup.tar /var/lib/mysql
Produces mysql_data_backup.tar in your current folder.

${PWD} ensures the backup is saved to the directory you run the command from.

powershell
# Restore MySQL volume from backup
docker run --rm -v jwt_mysql_data:/var/lib/mysql -v ${PWD}:/backup busybox tar xvf /backup/mysql_data_backup.tar -C /
Restores the volume from the tar file.

⚠️ Not portable — tied to MySQL’s internal storage format and version.

⚡ Alternative: SQL dump (portable backup)
This method exports the database contents as SQL statements. Use this when you want a portable backup or to migrate data to another MySQL instance.

powershell
# Backup one database (e_commerce)
docker exec ecommerce-mysql mysqldump -uroot -pKarthik@123 e_commerce > e_commerce_backup.sql
powershell
# Restore one database
docker exec -i ecommerce-mysql mysql -uroot -pKarthik@123 e_commerce < e_commerce_backup.sql
Works across different MySQL versions and environments.

Slower for very large datasets, but safer for long‑term storage and migration.

✅ When to use which
Volume backup (tar) → Fast snapshot, good for local dev/test rollback.

SQL dump (mysqldump) → Portable, flexible, good for production backups and migrations.





after docker compose--->
docker exec -it ollama ollama pull gemma:2b


