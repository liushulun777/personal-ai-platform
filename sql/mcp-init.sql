-- ============================================================
-- MCP 服务和工具初始化脚本
-- ============================================================

-- 1. 文件系统 MCP 服务
INSERT INTO mcp_server (id, name, description, transport_type, command, args, status, author_id) VALUES
(1001, 'filesystem', '文件系统操作服务', 'stdio', 'npx', '["-y", "@modelcontextprotocol/server-filesystem", "/tmp"]', 1, 1);

-- 文件系统工具
INSERT INTO mcp_tool (id, server_id, name, description, input_schema, status) VALUES
(2001, 1001, 'read_file', '读取文件内容', '{"type":"object","properties":{"path":{"type":"string","description":"文件路径"}},"required":["path"]}', 1),
(2002, 1001, 'write_file', '写入文件内容', '{"type":"object","properties":{"path":{"type":"string","description":"文件路径"},"content":{"type":"string","description":"文件内容"}},"required":["path","content"]}', 1),
(2003, 1001, 'create_directory', '创建目录', '{"type":"object","properties":{"path":{"type":"string","description":"目录路径"}},"required":["path"]}', 1),
(2004, 1001, 'delete_file', '删除文件或目录', '{"type":"object","properties":{"path":{"type":"string","description":"文件或目录路径"}},"required":["path"]}', 1);

-- 2. Git MCP 服务
INSERT INTO mcp_server (id, name, description, transport_type, command, args, status, author_id) VALUES
(1002, 'git', 'Git 版本控制服务', 'stdio', 'npx', '["-y", "@modelcontextprotocol/server-git"]', 1, 1);

-- Git 工具
INSERT INTO mcp_tool (id, server_id, name, description, input_schema, status) VALUES
(2005, 1002, 'git_clone', '克隆仓库', '{"type":"object","properties":{"repo_url":{"type":"string","description":"仓库地址"},"target_path":{"type":"string","description":"目标路径"}},"required":["repo_url","target_path"]}', 1),
(2006, 1002, 'git_commit', '提交代码', '{"type":"object","properties":{"message":{"type":"string","description":"提交信息"}},"required":["message"]}', 1),
(2007, 1002, 'git_checkout', '切换分支', '{"type":"object","properties":{"branch":{"type":"string","description":"分支名"}},"required":["branch"]}', 1),
(2008, 1002, 'git_diff', '查看差异', '{"type":"object","properties":{}}', 1);

-- 3. Terminal MCP 服务
INSERT INTO mcp_server (id, name, description, transport_type, command, args, status, author_id) VALUES
(1003, 'terminal', '终端命令执行服务', 'stdio', 'npx', '["-y", "@modelcontextprotocol/server-shell"]', 1, 1);

-- Terminal 工具
INSERT INTO mcp_tool (id, server_id, name, description, input_schema, status) VALUES
(2009, 1003, 'exec', '执行命令', '{"type":"object","properties":{"command":{"type":"string","description":"要执行的命令"},"work_dir":{"type":"string","description":"工作目录"}},"required":["command"]}', 1);

-- 4. Database MCP 服务
INSERT INTO mcp_server (id, name, description, transport_type, command, args, status, author_id) VALUES
(1004, 'database', '数据库操作服务', 'stdio', 'npx', '["-y", "@modelcontextprotocol/server-postgres", "postgresql://postgres:postgres@localhost:5432/platform"]', 1, 1);

-- Database 工具
INSERT INTO mcp_tool (id, server_id, name, description, input_schema, status) VALUES
(2010, 1004, 'query', '执行查询', '{"type":"object","properties":{"sql":{"type":"string","description":"SQL查询语句"}},"required":["sql"]}', 1),
(2011, 1004, 'execute', '执行更新', '{"type":"object","properties":{"sql":{"type":"string","description":"SQL更新语句"}},"required":["sql"]}', 1);

-- 5. HTTP MCP 服务
INSERT INTO mcp_server (id, name, description, transport_type, command, args, status, author_id) VALUES
(1005, 'http', 'HTTP 请求服务', 'stdio', 'npx', '["-y", "@modelcontextprotocol/server-fetch"]', 1, 1);

-- HTTP 工具
INSERT INTO mcp_tool (id, server_id, name, description, input_schema, status) VALUES
(2012, 1005, 'http_get', 'HTTP GET 请求', '{"type":"object","properties":{"url":{"type":"string","description":"请求URL"}},"required":["url"]}', 1),
(2013, 1005, 'http_post', 'HTTP POST 请求', '{"type":"object","properties":{"url":{"type":"string","description":"请求URL"},"body":{"type":"string","description":"请求体"}},"required":["url","body"]}', 1);

-- 6. Docker MCP 服务
INSERT INTO mcp_server (id, name, description, transport_type, command, args, status, author_id) VALUES
(1006, 'docker', 'Docker 容器服务', 'stdio', 'npx', '["-y", "@modelcontextprotocol/server-docker"]', 1, 1);

-- Docker 工具
INSERT INTO mcp_tool (id, server_id, name, description, input_schema, status) VALUES
(2014, 1006, 'docker_build', '构建镜像', '{"type":"object","properties":{"dockerfile":{"type":"string","description":"Dockerfile路径"},"tag":{"type":"string","description":"镜像标签"}},"required":["dockerfile","tag"]}', 1),
(2015, 1006, 'docker_run', '运行容器', '{"type":"object","properties":{"image":{"type":"string","description":"镜像名称"},"args":{"type":"string","description":"运行参数"}},"required":["image"]}', 1);

-- 7. Knowledge MCP 服务
INSERT INTO mcp_server (id, name, description, transport_type, endpoint, status, author_id) VALUES
(1007, 'knowledge', '知识库检索服务', 'sse', 'http://localhost:1087/knowledge', 1, 1);

-- Knowledge 工具
INSERT INTO mcp_tool (id, server_id, name, description, input_schema, status) VALUES
(2016, 1007, 'search', '知识库搜索', '{"type":"object","properties":{"query":{"type":"string","description":"搜索关键词"},"top_k":{"type":"integer","description":"返回数量","default":5}},"required":["query"]}', 1);

-- 完成
SELECT 'MCP 服务和工具初始化完成！' AS message;
