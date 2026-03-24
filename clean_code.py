import os
import re

# Emoji regex
emoji_pattern = re.compile(
    "["
    "\U00010000-\U0010FFFF"
    "\u2600-\u26FF"
    "\u2700-\u27BF"
    "\uFE0F"
    "\u200D"
    "]+", flags=re.UNICODE
)

ai_comment_pattern = re.compile(r'\bIA\b|Prompt\s+Engineering', re.IGNORECASE)

def clean_emojis(text):
    return emoji_pattern.sub('', text)

def process_content(content, ext):
    if ext in ['.java', '.js', '.css']:
        return process_java_js_css(content)
    elif ext == '.html':
        return process_html(content)
    return content

def process_java_js_css(content):
    # This regex matches strings, single-line comments, and multi-line comments.
    # Group 1: Double-quoted string
    # Group 2: Single-quoted string
    # Group 3: Single-line comment
    # Group 4: Multi-line comment
    # Group 5: Log/Alert function calls with string argument
    
    # We'll use a multi-step approach for clarity and safety.
    
    # First, handle logs and alerts specifically to clean strings inside them.
    log_pattern = re.compile(
        r'(?P<func>(logger\.(info|error|warn|debug|trace|log)|console\.(log|error|warn|info|debug)|alert)\s*\(\s*)'
        r'(?P<quote>["\'])(?P<text>(?:\\.|(?!(?P=quote)).)*)(?P<suffix>(?P=quote))',
        re.DOTALL
    )
    
    content = log_pattern.sub(lambda m: m.group('func') + m.group('quote') + clean_emojis(m.group('text')) + m.group('suffix'), content)

    # Then, handle comments. To avoid matching strings, we match them and return them unchanged.
    pattern = re.compile(
        r'("(?:\\.|[^"\\])*")|'          # Group 1: Double-quoted string
        r"('(?:\\.|[^'\\])*')|"          # Group 2: Single-quoted string
        r'(//.*)|'                       # Group 3: Single-line comment
        r'(/\*[\s\S]*?\*/)',             # Group 4: Multi-line comment
        re.MULTILINE
    )

    def replace_comments(match):
        if match.group(1): return match.group(1) # String
        if match.group(2): return match.group(2) # String
        comment = match.group(3) or match.group(4)
        if comment:
            if ai_comment_pattern.search(comment):
                return "" # Remove IA comments
            return clean_emojis(comment)
        return match.group(0)

    return pattern.sub(replace_comments, content)

def process_html(content):
    # 1. Clean HTML comments
    def repl_html_comment(match):
        comment = match.group(0)
        if ai_comment_pattern.search(comment):
            return ""
        return clean_emojis(comment)
    
    content = re.sub(r'<!--[\s\S]*?-->', repl_html_comment, content)
    
    # 2. Clean JS inside <script> tags
    def repl_script(match):
        attrs = match.group(1)
        script_code = match.group(2)
        cleaned_code = process_java_js_css(script_code)
        return f"<script{attrs}>{cleaned_code}</script>"
    
    content = re.sub(r'<script([^>]*)>([\s\S]*?)</script>', repl_script, content, flags=re.IGNORECASE)

    # 3. Clean CSS inside <style> tags
    def repl_style(match):
        attrs = match.group(1)
        style_code = match.group(2)
        cleaned_code = process_java_js_css(style_code) # CSS uses the same comment style /* */
        return f"<style{attrs}>{cleaned_code}</style>"

    content = re.sub(r'<style([^>]*)>([\s\S]*?)</style>', repl_style, content, flags=re.IGNORECASE)
    
    # 4. Clean alerts in attributes (e.g. onclick="alert('...')")
    def repl_inline_alert(match):
        prefix = match.group(1)
        quote = match.group(2)
        text = match.group(3)
        return f"{prefix}{quote}{clean_emojis(text)}"
    
    content = re.sub(r'(alert\s*\(\s*)(["\'])(.*?)(?=\2\s*\))', repl_inline_alert, content)
    
    return content

def main():
    target_dirs = ['backend/src', 'frontend']
    modified_count = 0
    for t_dir in target_dirs:
        abs_t_dir = os.path.join('/home/atlas/Bureau/DEVOPS/DataScale', t_dir)
        for root, dirs, files in os.walk(abs_t_dir):
            for f in files:
                if f.endswith(('.java', '.js', '.html', '.css')):
                    f_path = os.path.join(root, f)
                    try:
                        with open(f_path, 'r', encoding='utf-8') as fh:
                            content = fh.read()
                        
                        ext = os.path.splitext(f)[1]
                        new_content = process_content(content, ext)
                        
                        if new_content != content:
                            with open(f_path, 'w', encoding='utf-8') as fh:
                                fh.write(new_content)
                            modified_count += 1
                            print(f"Modified: {f_path}")
                    except Exception as e:
                        print(f"Error processing {f_path}: {e}")
    print(f"Total modified: {modified_count}")

if __name__ == "__main__":
    main()
