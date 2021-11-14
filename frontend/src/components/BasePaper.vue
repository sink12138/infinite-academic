<template>
<div class="outer">
    <div class="main">
        <div class="title">{{paperdata.title}}</div>
        <ul class="author wrapper">
            <div class="lefttitle">作者:</div>
            <div class="rightcontent" style="display:flex">
                <li v-for="item in paperdata.authors" v-bind:key="item.id">
                    <a>{{item[0][1]}}</a>&nbsp;&nbsp;
                </li>
            </div>
        </ul>
        <div class="wrapper">
            <div class="lefttitle">摘要:</div>
            <div class="rightcontent">{{paperdata.abstract}}</div>
        </div>
    <ul class="keywords wrapper">
        <div class="lefttitle">关键词:</div>
        <div class="rightcontent" style="display:flex">
            <li v-for="item in paperdata.keywords" v-bind:key="item.id">
               {{item}}&nbsp;&nbsp;
            </li>
        </div>
    </ul>
    <div class="doi wrapper">
        <div class="lefttitle">DOI:</div>
        <div class="rightcontent">
            {{paperdata.doi}}
        </div>
    </div>
    <div class="issn wrapper">
        <div class="lefttitle">ISSN:</div>
        <div class="rightcontent">
            {{paperdata.issn}}
        </div>
    </div>
    <div class="isbn wrapper">
        <div class="lefttitle">ISBN:</div>
        <div class="rightcontent">
            {{paperdata.isbn}}
        </div>
    </div>
    <div class="citationNum wrapper">
        <div class="lefttitle">被引量:</div>
        <div class="rightcontent">
            {{paperdata.citationNum}}
        </div>
    </div>
    <div class="year wrapper">
        <div class="lefttitle">年份:</div>
        <div class="rightcontent">
            {{paperdata.year}}
        </div>
    </div>
    <ul class="references wrapper">
        <div class="lefttitle">参考文献:</div>
        <div class="rightcontent">
            <li v-for="item in paperdata.references" v-bind:key="item.id">
               <a>{{item}}</a>&nbsp;&nbsp;
            </li>
        </div>
    </ul>
    <ul class="url wrapper">
        <div class="lefttitle">论文来源:</div>
        <div class="rightcontent">
            <li v-for="item in paperdata.url" v-bind:key="item.id">
               <a>{{item}}</a>&nbsp;&nbsp;
            </li>
        </div>
    </ul>
    </div>
    <div class="right">
        <div class="right-title">论文所属期刊</div>
        <div class="right-content">
        <div>
            {{paperdata.journal.name}}
        </div>
        <div>
            第{{paperdata.journal.volume}}卷第{{paperdata.journal.issue}}期{{paperdata.journal.startPage}}页到{{paperdata.journal.endPage}}页
        </div>
        论文出版单位:{{paperdata.publisher}}
        </div>
    </div>
</div>
</template>
<script>
export default{
    props:{
        paperdata:{}
    },
    data(){
        return{

        }
    }
}
</script>
<style scoped>
ul{
    margin:0;
    padding:0;
}
.outer{
    width:100%;
    display: flex;
    text-decoration: none;
    padding-left:10%;
    padding-right:10%;
    line-height: 2.5;
}
.outer li{
    list-style:none;
}
.main{
    width:75%;
    /* background-color: antiquewhite; */
    padding: 10px;
}
.title{
    color:#06c;
    font-size:24px;
}
.right{
    width: 25%;
    /* background-color: burlywood; */
    padding:10px;
}
.flexbox{
    display:flex;
}

.wrapper{
    width: 100%;
    display: flex;
}
.lefttitle{
    flex-basis:80px;
    text-align:left;
    font:14px Microsoft YaHei,Arial,Helvetica,sans-serif;
    line-height: 2.7;
}
.rightcontent{
    flex-basis:200px;
    flex-grow:2;
    text-align: left;
}
.right-title{
    margin-bottom: 18px;
    font-weight: initial;
    font-size: 18px;
    color: #333;
}
.right-content{
    width: 70%;
    max-height: 60px;
    display: block;
    padding-bottom: 10px;
    overflow: hidden;
    font-size: 14px;
    color: #333;
}
</style>