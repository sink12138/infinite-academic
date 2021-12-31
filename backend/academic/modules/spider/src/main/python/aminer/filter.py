def fields(obj: dict) -> bool:
    if 'id' not in obj:
        return False
    if 'title' not in obj or len(obj['title']) < 64 or obj['title'].count(' ') <= 2:
        return False
    if 'keywords' not in obj or len(obj['keywords']) == 0:
        return False
    if 'abstract' not in obj:
        return False
    elif len(obj['abstract']) > 2048:
        obj['abstract'] = obj['abstract'][:2048] + '...'
    if 'authors' not in obj or len(obj['authors']) == 0:
        return False
    return True


def terms(obj: dict, keywords: list) -> bool:
    for keyword in keywords:
        if 'title' in obj and (keyword in obj['title'] or keyword.title() in obj['title']):
            return True
        if 'abstract' in obj and keyword in obj['abstract']:
            return True
        if 'keywords' in obj and (keyword in obj['keywords'] or keyword.title() in obj['keywords']):
            return True
    return False


def orgs(obj: dict) -> bool:
    return 'name' in obj and ('org' in obj or 'orgs' in obj and len(obj['orgs']) > 0 and obj['orgs'][0] != '')


def title(obj: dict) -> bool:
    return 'DisplayName' in obj or 'NormalizedName' in obj
