import os
import subprocess

def get_flyway_files(branch):
    # 브랜치 체크아웃
    subprocess.run(['git', 'checkout', branch], check=True)
    # db 디렉토리에서 파일 목록 가져오기
    db_path = os.path.join(os.getcwd(), 'src/main/resources/db/migration')
    files = os.listdir(db_path)
    # Flyway 파일 추출 및 정렬
    flyway_files = sorted([f for f in files if f.startswith('V')])
    return flyway_files

def extract_version_and_name(file_name):
    version, name = file_name.split('__', 1)
    return version, name

def main():

    # 현재 브랜치의 Flyway 파일 가져오기
    current_branch = subprocess.check_output(['git', 'rev-parse', '--abbrev-ref', 'HEAD']).strip().decode('utf-8')
    pr_files = get_flyway_files(current_branch)

    # 메인 브랜치의 Flyway 파일 가져오기
    main_files = get_flyway_files('develop')
    main_versions = {}
    for file_name in main_files:
        version, name = extract_version_and_name(file_name)
        main_versions[version] = name
    
    conflict_versions = []
    for file_name in pr_files:
        version, name = file_name.split('__', 1)
        
        if version in main_versions and name != main_versions[version]:
            conflict_versions.append(file_name)

    if conflict_versions:
        print(f"Error: Version conflict(s) found for versions: {', '.join(conflict_versions)}")
        exit(1)
    else:
        print("No version conflicts found.")

if __name__ == "__main__":
    main()
