#!/bin/bash
#
# Script para limpar simulacoes antigas

reset
echo "Removendo arquivo .amostras..."
rm -f /home/lucio/modeloLingo*.amostras
echo "Removendo arquivo .banda..."
rm -f /home/lucio/modeloLingo*.banda
echo "Removendo arquivo .banda_..."
rm -f /home/lucio/modeloLingo*.banda_*
echo "Removendo arquivo .banda_..."
rm -f /home/lucio/modeloLingo*.datacenter
echo "Removendo arquivo .datacenter_..."
rm -f /home/lucio/modeloLingo*.datacenter_*
echo "Removendo arquivo .lg4..."
rm -f /home/lucio/modeloLingo*.lg4
echo "Removendo arquivo .lg4_..."
rm -f /home/lucio/modeloLingo*.lg4_*
echo "Removendo arquivo .lgr..."
rm -f /home/lucio/modeloLingo*.lgr
echo "Removendo arquivo .lgr_..."
rm -f /home/lucio/modeloLingo*.lgr_*
echo "Removendo arquivo .parser..."
rm -f /home/lucio/modeloLingo*.parser
echo "Removendo arquivo .parser_..."
rm -f /home/lucio/modeloLingo*.parser_*
echo "Removendo arquivo .range..."
rm -f /home/lucio/modeloLingo*.range
echo "Removendo arquivo .evolucao*..."
rm -f /home/lucio/modeloLingo*.evolucao*
echo "Removendo arquivo NS2..."
rm -f /home/lucio/modeloNS2*
echo "Clear all files: *.allocs..."
rm -f /home/lucio/modeloLingo*.allocs
echo "Clear all files: *.progresso..."
rm -f /home/lucio/modeloLingo*.progresso
echo "Clear all files: *.amountAllocs..."
rm -f /home/lucio/modeloLingo*.amountAllocs
echo "Clear all files: *.amountFlows..."
rm -f /home/lucio/modeloLingo*.amountFlows
echo "Clear all files: modeloFF_*...."
rm -f /home/lucio/modeloFF_*
echo "Clear all files: modeloNF_*...."
rm -f /home/lucio/modeloNF_*
