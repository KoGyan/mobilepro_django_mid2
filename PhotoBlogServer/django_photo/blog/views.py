from django.shortcuts import get_object_or_404, redirect, render
from django.utils import timezone
from blog.form import PostForm
from .models import Post
from rest_framework import viewsets
from .serializers import PostSerializer
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from datetime import datetime
import os
from rest_framework.views import APIView
from mysite import settings


def post_list(request):
    posts = Post.objects.filter(published_date__lte=timezone.now()).order_by('published_date')
    return render(request, 'blog/post_list.html', {'posts': posts})

# Create your views here.
def post_new(request):
    if request.method == "POST":
        form = PostForm(request.POST)
        if form.is_valid():
            post = form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    else:
        form = PostForm()
    return render(request, 'blog/post_edit.html', {'form': form})

def post_detail(request, pk):
    post = get_object_or_404(Post, pk=pk)
    return render(request, 'blog/post_detail.html', {'post': post})

def post_edit(request, pk):
    post = get_object_or_404(Post, pk=pk)
    if request.method == "POST":
        form = PostForm(request.POST, instance=post)
        if form.is_valid():
            post = form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()

            return redirect('post_detail', pk=post.pk)
    else:
        form = PostForm(instance=post)

    return render(request, 'blog/post_edit.html', {'form': form})

def js_test(request):
    return render(request, 'blog/js_test.html')

class DeletePostAPIView(APIView):
    def delete(self, request, *args, **kwargs):
        title = request.GET.get('title', None)  # URL에서 title 가져오기
        if not title:
            return JsonResponse({"error": "Title parameter is required"}, status=400)

        try:
            post = Post.objects.get(title=title)  # title로 Post 검색
            post.delete()  # Post 삭제
            return JsonResponse({"success": "Image deleted successfully"}, status=200)
        except Post.DoesNotExist:
            return JsonResponse({"error": "Image with the given title does not exist"}, status=404)

class BlogImages(viewsets.ModelViewSet):
    queryset = Post.objects.all()
    serializer_class = PostSerializer

