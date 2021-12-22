<template>
  <v-card
    calss="rounded-lg"
    elevation=6
    width=100%
  >
    <div class="whole">
      <v-row>
        <v-col cols="10">
          <v-row v-if="edit">
            <v-col >
              <v-textarea
                label="修改原因和描述"
                v-model="discription"
              ></v-textarea>
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-text-field
                v-model="title"
                label="标题"
                required
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row>
            <v-col >
              <v-textarea
                label="摘要"
                v-model="abstract"
              ></v-textarea>
            </v-col>
          </v-row>
          <v-row>
            <v-col v-for="i in keywords.length" :key="i" cols="6">
              <v-row>
                <v-col>
                  <v-text-field
                    label="关键词"
                    v-model="keywords[i-1]"
                  ></v-text-field>
                </v-col>
                <v-col>
                  <v-btn @click="deleteKeyword(i-1)">
                    删除该项
                  </v-btn>
                </v-col>
              </v-row>
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-btn @click="addKeyword()">
                添加关键词
              </v-btn>
            </v-col>
          </v-row>
          <v-row>
            <v-col v-for="i in subjects.length" :key="i" cols="6">
              <v-row>
                <v-col>
                  <v-text-field
                    label="学科分类"
                    v-model="subjects[i-1]"
                  ></v-text-field>
                </v-col>
                <v-col>
                  <v-btn @click="deleteSubject(i-1)">
                    删除该项
                  </v-btn>
                </v-col>
              </v-row>
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-btn @click="addSubject()">
                添加学科分类
              </v-btn>
            </v-col>
            
          </v-row>
          
          <v-row>
            <v-select
              :items="types"
              label="文献类型"
              v-model="type"
              solo
            ></v-select>
          </v-row>
          <v-row v-for="i in authors.length" :key="i">
            <v-col cols="3">
              <v-text-field
                v-model="authors[i-1].id"
                label="作者id"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="3">
              <v-text-field
                v-model="authors[i-1].name"
                label="作者姓名"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="3">
              <v-text-field
                v-model="authors[i-1].instName"
                label="作者所在机构名"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="2">
              <v-btn @click="deleteAuthor(i-1)">
                删除该项
              </v-btn>
            </v-col>
          </v-row>
          <v-btn @click="addAuthor()">
            添加作者
          </v-btn>
          <v-row>
            <v-col>
              <p>发表日期:</p>
              <v-date-picker v-model="date"></v-date-picker>
            </v-col>
          </v-row>
          
          <v-row>
            <v-col >
              <v-text-field
                label="DOI"
                v-model="doi"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row v-for="i in institutions.length" :key="i">
            <v-col cols="5">
              <v-text-field
                v-model="institutions[i-1].id"
                label="机构id"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="5">
              <v-text-field
                v-model="institutions[i-1].name"
                label="机构名称"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="2">
              <v-btn @click="deleteI(i-1)">
                删除该项
              </v-btn>
            </v-col>
          </v-row>
          <v-btn @click="addInst()">
            添加机构
          </v-btn>
          <v-row>
            <v-col cols="6">
              <v-text-field
                label="期刊ID"
                v-model="journal.id"
              ></v-text-field>
            </v-col>
            <v-col cols="6">
              <v-text-field
                label="期刊标题"
                v-model="journal.title"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="6">
              <v-text-field
                label="论文在期刊的期号"
                v-model="journal.issue"
              ></v-text-field>
            </v-col>
            <v-col cols="6">
              <v-text-field
                label="论文在期刊的卷号"
                v-model="journal.volume"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row>
            <v-col >
              <v-text-field
                label="论文在期刊中的起始页码"
                v-model="journal.startPage"
              ></v-text-field>
            </v-col>
            <v-col >
              <v-text-field
                label="论文在期刊中的终止页码"
                v-model="journal.endPage"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row>
            <v-col >
              <v-text-field
                label="出版商"
                v-model="publisher"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row v-for="i in referencePapers.length" :key="i">
            <v-col cols="5">
              <v-text-field
                v-model="referencePapers[i-1].id"
                label="引用文献id"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="5">
              <v-text-field
                v-model="referencePapers[i-1].title"
                label="引用文献标题"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="2">
              <v-btn @click="deleteReferencePapers(i-1)">
                删除该项
              </v-btn>
            </v-col>
          </v-row>
          <v-btn @click="addReferencePapers()">
            添加引用文献
          </v-btn>
          <v-row>
            <v-text-field
                v-model="year"
                label="发表年份"
                required
              ></v-text-field>
          </v-row>
          <v-row>
            <v-col >
              <v-text-field
                v-model="websiteLink"
                label="证明材料链接"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-file-input 
              chips 
              label="上传证明文件"
              ></v-file-input>
            </v-col>
          </v-row>
          <v-row>
            <v-col v-if="edit" cols="6">
              <v-btn @click="submitEditPaper()">
                提交修改论文申请
              </v-btn>
            </v-col>
            <v-col v-else cols="6">
              <v-btn @click="submitAddPaper()">
                提交添加论文申请
              </v-btn>
            </v-col>
            <v-col cols="6">
              <v-btn @click="cancel()">
                取消
              </v-btn>
            </v-col>
          </v-row> 
        </v-col>
        <v-col>
          <div class="fixBut">
            <v-btn @click="getID=true">相关信息ID查询</v-btn>
          </div>
          
        </v-col>
      </v-row>
      
     
    </div>
    <v-dialog v-model="getID" persistent width=1200px >
      <v-card height=5000px>
        <Search
          :fromDoor="disabled"
          @closeID="closeID"
        ></Search>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script>
  // import BaseGetID from './BaseGetID.vue'
  import Search from './Search.vue'
  export default {
    components: {Search},
    props:{
      paper:Object,
      edit:Boolean,
      email:String,
    },
    data(){
      return{
        title:this.paper.title,
        id:this.paper.id,
        abstract:this.paper.abstract,
        authors:this.paper.authors,
        date:this.paper.date,
        doi:this.paper.doi,
        institutions:this.paper.institutions,
        journal:this.paper.journal,
        keywords:this.paper.keywords,
        publisher:this.paper.publisher,
        referencePapers:this.paper.referencePapers,
        subjects:this.paper.subjects,
        type:this.paper.type,
        year:this.paper.year,
        types:["图书","学位论文","期刊论文"],
        close:"",
        discription:"",
        fileToken:"",
        websiteLink:"",
        getID:false,
      }
    },
    methods:{
      addAuthor(){
        this.authors.push({
          id:"",
          name:"",
          instName:""
        })
      },
      deleteAuthor(index){
        this.authors.splice(index, 1)
      },
      addKeyword(){
        this.keywords.push("")
      },
      deleteKeyword(index){
        this.keywords.splice(index, 1)
      },
      addSubject(){
        this.subjects.push("")
      },
      deleteSubject(index){
        this.subjects.splice(index, 1)
      },
      addReferencePapers(){
        this.referencePapers.push({
          id:"",
          title:""
        })
      },
      deleteReferencePapers(index){
        this.referencePapers.splice(index, 1)
      },
      addInst(){
        this.institutions.push({
          id:"",
          name:""
        })
      },
      deleteI(index){
        this.institutions.splice(index, 1)
      },
      submitAddPaper(){
        this.$axios({
          method: "post",
          url: "/api/scholar/paper/add",
          params: {
            paper:{
              content:{
                add:{
                  abstract:this.abstract,
                  authors:this.authors,
                  date:this.date,
                  doi:this.doi,
                  institutions:this.institutions,
                  journal:this.journal,
                  keywords:this.keywords,
                  publisher:this.publisher,
                  subjects:this.subjects,
                  referencePapers:this.referencePapers,
                  title:this.title,
                  type:this.type,
                  year:this.year,
                },
              },
              email:this.email,
              websiteLink:this.websiteLink,
              fileToken:this.fileToken
            }
          }
        }).then(response => {
          console.log(response.data)
          // this.snackbarSub=true
          this.$emit('closeZ',"close")
        }).catch(error => {
          console.log(error)
        })
      },
      submitEditPaper(){
        this.$axios({
          method: "post",
          url: "/api/scholar/paper/edit",
          params: {
            paper:{
              content:{
                discription:this.description,
                paperId:this.id,
                edit:{
                  abstract:this.abstract,
                  authors:this.authors,
                  date:this.date,
                  doi:this.doi,
                  institutions:this.institutions,
                  journal:this.journal,
                  keywords:this.keywords,
                  publisher:this.publisher,
                  subjects:this.subjects,
                  referencePapers:this.referencePapers,
                  title:this.title,
                  type:this.type,
                  year:this.year,
                },
              },
              email:this.email,
              websiteLink:this.websiteLink,
              fileToken:this.fileToken
            }
          }
        }).then(response => {
          console.log(response.data)
          // this.snackbarSub=true
          this.$emit('closeZ',"close")
        }).catch(error => {
          console.log(error)
        })
      },
      cancel(){
        this.$emit('closeZ',"close")
      },
      closeID(msg){
        if(msg=="close")
          this.getID=false
      }
    }
  }
</script>
<style>
.fixBut{
    position: fixed;
    top:60px;
    right:25%;
    z-index:9999;
}
</style>