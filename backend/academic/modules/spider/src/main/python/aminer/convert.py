import re


def org_name(src: str) -> str:
    parts = re.split(r'\s+', src.strip())
    for i in range(2, len(parts)):
        if re.findall(r'\d{5,}', parts[i]):
            parts = parts[:i]
            break
    institution = ', '.join(re.split(r'\s*[|,]\s*', ' '.join(parts)))
    if len(institution) > 48 and ',' in institution:
        parts = re.split(r',\s*', institution)
        joined = [parts[0]]
        for part in parts[1:]:
            if not re.findall(r'\d{5,}', part):
                joined.append(part)
                break
        institution = ', '.join(re.split(r',\s*', institution)[:2])
    return institution


def paper(src: dict) -> dict:
    res = {
        # id
        'id': src['id'],
        # title
        'title': src['title']
    }
    # type
    if 'doc_type' in src:
        if src['doc_type'] == 'journal':
            res['type'] = '期刊论文'
        elif src['doc_type'] == 'book':
            res['type'] = '图书'
    elif 'venue' in src and 'raw' in src['venue']:
        if 'conference' in src['venue']['raw'] or 'Conference' in src['venue']['raw']:
            res['type'] = '会议论文'
        else:
            res['type'] = '期刊论文'
    # authors & institutions
    if 'authors' in src:
        res['authors'] = []
        institutions = []
        for author in src['authors']:
            if 'name' not in author:
                continue
            paper_author = {}
            if 'id' in author:
                paper_author['id'] = author['id']
            paper_author['name'] = author['name']
            if 'org' in author:
                institution = org_name(author['org'])
                paper_author['name'] = '@@'.join([paper_author['name'], institution])
                if institution not in institutions:
                    institutions.append(institution)
            res['authors'].append(paper_author)
        if len(institutions) > 0:
            res['institutions'] = []
            for institution in institutions:
                res['institutions'].append({
                    'name': institution
                })
    # abstract
    if 'abstract' in src:
        if len(src['abstract']) > 1024:
            res['paperAbstract'] = src['abstract'][:1024]
        else:
            res['paperAbstract'] = src['abstract']
    # keywords
    if 'keywords' in src:
        res['keywords'] = src['keywords']
    # year
    if 'year' in src:
        year = src['year']
        res['year'] = year
        res['date'] = f'{year}-01-01'
    # doi
    if 'doi' in src:
        res['doi'] = src['doi']
    # journal
    if 'type' in res and res['type'] == '期刊论文' and 'venue' in src and 'id' in src['venue']:
        res['journal'] = {}
        res['journal']['id'] = src['venue']['id']
        res['journal']['title'] = src['venue']['raw']
        if 'volume' in src and src['volume'] != '':
            res['journal']['volume'] = re.findall(r'[A-Za-z\d/.]+', src['volume'])[0]
            if 'issue' in src and src['issue'] != '':
                res['journal']['issue'] = re.findall(r'[A-Za-z\d/.]+', src['issue'])[0]
        if 'page_start' in src and 'page_end' and src['page_start'] != '' and src['page_end'] != '' in src:
            res['journal']['startPage'] = int(src['page_start'])
            res['journal']['endPage'] = int(src['page_end'])
    # publisher
    if 'publisher' in src:
        res['publisher'] = src['publisher']
    # source
    websites = []
    links = []
    if 'pdf' in src:
        if src['pdf'][:2] == '//':
            websites.append(src['pdf'].split('/')[2])
            links.append(src['pdf'][2:])
    if 'url' in src:
        i = 0
        for link in src['url']:
            if i >= 8:
                break
            if link[:8] == 'https://' or link[:7] == 'http://':
                website = link.split('/')[2]
                if website not in websites:
                    websites.append(website)
                    links.append(link)
                    i += 1
    if len(websites) > 0:
        res['sources'] = []
        for i in range(len(websites)):
            res['sources'].append({
                'website': websites[i],
                'url': links[i]
            })
    return res


def researcher(src: dict) -> dict:
    res = {
        # id
        'id': src['id'],
        # name
        'name': src['name']
    }
    # currentInst
    if 'org' in src and src['org'] != '':
        res['currentInst'] = {
            'name': org_name(src['org'])
        }
    # institutions
    if 'orgs' in src:
        institutions = []
        for org in src['orgs']:
            if org == '':
                continue
            institution = org_name(org)
            if 'currentInst' in res and institution == res['currentInst']['name']:
                continue
            if institution not in institutions:
                institutions.append(institution)
        if len(institutions) > 0:
            res['institutions'] = []
            for institution in institutions:
                res['institutions'].append({
                    'name': institution
                })
    # hIndex
    if 'h_index' in src:
        res['hIndex'] = src['h_index']
    # interests
    if 'tags' in src:
        res['interests'] = []
        for tag in src['tags']:
            if len(tag['t']) > 64:
                continue
            res['interests'].append(tag['t'])
    return res


def journal(src: dict) -> dict:
    res = {
        # id
        'id': src['id']
    }
    # title
    if 'DisplayName' in src:
        res['title'] = src['DisplayName']
    else:
        res['title'] = src['NormalizedName']
    return res
