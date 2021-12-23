import json
import os.path
import sys

import convert
import filter
from reader import Reader


keywords = [
    'algorithm',
    'database',
    'data mining',
    'machine learning',
    'artificial intelligence',
    'natural language processing',
    'cloud computing',
    'big data',
    'style transfer',
    'operating system',
    'deep learning',
    'search engine',
]


def select_paper(_in, _out, _step):
    # if not os.path.exists(_out):
    #     os.makedirs(_out)
    with open(_in, 'r', encoding='utf-8') as file:
        reader = Reader(file, filters=[
            filter.fields,
            lambda x: filter.terms(x, keywords)
        ])
        with open(_out, 'a', encoding='utf-8') as out:
            obj = reader.next_object()
            num = 0
            while obj:
                paper = convert.paper(obj)
                print(json.dumps(paper, ensure_ascii=False), file=out)
                num += 1
                if num % _step == 0:
                    print(f"{reader.count} scanned, {num} selected ({'%.2f%%' % (num / reader.count * 100)})")
                obj = reader.next_object()
        print(f"Finished - {reader.count} scanned, {num} selected ({'%.2f%%' % (num / reader.count * 100)})")


def select_researcher(_in, _out, _step):
    if not os.path.exists(_out):
        os.makedirs(_out)
    with open(_in, 'r', encoding='utf-8') as file:
        reader = Reader(file, filters=[
            filter.orgs
        ])
        obj = reader.next_object()
        num = 0
        while obj:
            researcher = convert.researcher(obj)
            filename = obj['id']
            with open(os.path.join(_out, f'{filename[:20]}'), 'w', encoding='utf-8') as out:
                print(json.dumps(researcher, ensure_ascii=False), file=out)
            num += 1
            if num % _step == 0:
                print(f"{reader.count} scanned, {num} selected ({'%.2f%%' % (num / reader.count * 100)})")
            obj = reader.next_object()
        print(f"Finished - {reader.count} scanned, {num} selected ({'%.2f%%' % (num / reader.count * 100)})")


def select_journal(_in, _out, _step):
    if not os.path.exists(_out):
        os.makedirs(_out)
    with open(_in, 'r', encoding='utf-8') as file:
        reader = Reader(file, filters=[
            filter.title
        ])
        obj = reader.next_object()
        num = 0
        while obj:
            journal = convert.journal(obj)
            filename = obj['id']
            with open(os.path.join(_out, f'{filename}'), 'a', encoding='utf-8') as out:
                print(json.dumps(journal, ensure_ascii=False), file=out)
            num += 1
            if num % _step == 0:
                print(f"{reader.count} scanned, {num} selected ({'%.2f%%' % (num / reader.count * 100)})")
            obj = reader.next_object()
        print(f"Finished - {reader.count} scanned, {num} selected ({'%.2f%%' % (num / reader.count * 100)})")


# if __name__ == '__main__':
#     input_file = 'F:\\aminerv2\\unzipped\\'  # sys.argv[1]
#     output_file = 'D:\\Aminer\\output\\'  # sys.argv[2]
#     for i in range(0, 15):
#         select_paper(f'{input_file}aminer_papers_{i}.txt', f'{output_file}aminer_papers_{i // 5}.txt', 10000)

if __name__ == '__main__':
    input_path = sys.argv[1]
    output_path = sys.argv[2]
    for i in range(10, 15):
        input_file = os.path.join(input_path, f'aminer_authors_{i}.txt')
        select_researcher(input_file, output_path, 100000)
        os.system(f'rm {input_file}')
